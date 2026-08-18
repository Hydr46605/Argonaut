package it.hydr4.argonaut.testing

import it.hydr4.argo.api.ArgoHttpEngine
import it.hydr4.argo.api.ArgoHttpRequest
import it.hydr4.argo.api.ArgoHttpResponse

/** Loads sanitized recorded payloads from `src/test/resources/fixtures/`. */
object Fixtures {
    fun text(name: String): String = checkNotNull(Fixtures::class.java.getResourceAsStream("/fixtures/$name")) {
        "Missing fixture 'fixtures/$name' on the test classpath"
    }.readBytes().decodeToString()
}

/**
 * Deterministic in-memory [ArgoHttpEngine] replaying canned responses — the
 * "fake Argos client" of the integration tests. Routes are (url-fragment,
 * handler) pairs; the first fragment contained in the request URL wins.
 * Every executed request is recorded for transport assertions.
 */
class FakeArgoEngine(vararg routes: Pair<String, (ArgoHttpRequest) -> ArgoHttpResponse>) : ArgoHttpEngine {

    private val routes: List<Pair<String, (ArgoHttpRequest) -> ArgoHttpResponse>> = routes.toList()

    val requests: MutableList<ArgoHttpRequest> = mutableListOf()

    @Suppress("ReturnCount")
    override suspend fun execute(request: ArgoHttpRequest): ArgoHttpResponse {
        var current = request
        repeat(MAX_REDIRECT_HOPS + 1) { hop ->
            requests += current
            val handler = routes.firstOrNull { (needle, _) -> current.url.contains(needle) }?.second
                ?: throw AssertionError("FakeArgoEngine: no route matches ${current.method} ${current.url}")
            val response = handler(current)
            if (!request.followRedirects || hop == MAX_REDIRECT_HOPS) return response
            val location = response.header("location") ?: return response
            val target = runCatching { java.net.URI(location) }.getOrNull() ?: return response
            if (target.scheme !in setOf("http", "https")) return response
            current = current.copy(url = java.net.URI(current.url).resolve(target).toString())
        }
        error("FakeArgoEngine: too many redirect hops for ${request.url}")
    }

    fun requestsTo(needle: String): List<ArgoHttpRequest> {
        val matched = requests.filter { it.url.contains(needle) }
        check(matched.isNotEmpty()) { "FakeArgoEngine: expected at least one request to '$needle', got none" }
        return matched
    }

    companion object {
        private const val MAX_REDIRECT_HOPS = 10

        /** Fixed server date so token-expiry math stays deterministic. */
        const val SERVER_DATE: String = "Tue, 25 Aug 2026 08:00:00 GMT"

        val SERVER_INSTANT: java.time.Instant = java.time.Instant.parse("2026-08-25T08:00:00Z")

        fun json(body: String, status: Int = 200): ArgoHttpResponse = ArgoHttpResponse(
            status,
            ArgoHttpResponse.headersOf("Date" to SERVER_DATE, "content-type" to "application/json"),
            body,
        )

        fun redirect(location: String): ArgoHttpResponse = ArgoHttpResponse(
            302,
            ArgoHttpResponse.headersOf("Location" to location, "Date" to SERVER_DATE),
            "",
        )
    }
}
