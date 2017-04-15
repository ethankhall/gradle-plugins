defineVersion("kotlin", "1.1.1").lock { withGroup("org.jetbrains.kotlin") }

defineLibrary("kotlin", "org.jetbrains.kotlin:kotlin-stdlib:${usingVersion("kotlin")}")