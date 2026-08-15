# Argos is a small, stable library consumed as a prebuilt jar. Its annotation
# registry reads class metadata at runtime (kotlin-reflect) and its DTOs are
# decoded via generated kotlinx.serialization serializers, so keep everything.
-keep,includedescriptorclasses class it.hydr4.argo.** { *; }

# kotlin-reflect: Argos's EndpointRegistry reads annotations via KClass metadata.
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Glance widget receivers are referenced by the system via class name.
-keep class it.hydr4.argonaut.widget.** { *; }
