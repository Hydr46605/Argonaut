package it.hydr4.argonaut.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Argonaut shape system: rounded corners used expressively to build hierarchy.
 *
 * - [extraLarge] rounds the hero average card (the visual anchor of the app);
 * - [large] rounds the standard cards;
 * - [medium]/[small] round list items, chips and inputs.
 */
internal val ArgonautShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)
