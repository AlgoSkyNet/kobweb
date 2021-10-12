package com.varabyte.kobweb.gradle.application.templates

import com.varabyte.kobweb.gradle.application.BuildTarget

fun createMainFunction(appFqcn: String?, pageFqcnRoutes: Map<String, String>, target: BuildTarget): String {
    val imports = mutableListOf(
        "com.varabyte.kobweb.core.Router",
        "kotlinx.browser.document",
        "kotlinx.browser.window",
        "org.jetbrains.compose.web.renderComposable",
    )

    if (target == BuildTarget.DEBUG) {
        imports.add("kotlinx.dom.hasClass")
        imports.add("kotlinx.dom.removeClass")
        imports.add("com.varabyte.kobweb.compose.css.*")
        imports.add("org.jetbrains.compose.web.css.*")
    }

    imports.add(appFqcn ?: "com.varabyte.kobweb.core.DefaultApp")
    imports.sort()

    return """
        ${
            imports.joinToString("\n        ") { fcqn -> "import $fcqn" }
        }

        private fun forceReloadNow() {
            window.stop()
            window.location.reload()
        }

        // In production, this will not be called and will get stripped out of the final javascript
        private fun pollServerStatus() {
            run {
                val status = document.getElementById("status")!!
                val statusText = document.getElementById("status_text")!!
                var lastStatus: String = ""
                var lastVersion: Int? = null
                var shouldReload = false

                status.addEventListener("transitionend", {
                    if (status.hasClass("fade-out")) {
                        status.removeClass("fade-out")
                        if (shouldReload) {
                            forceReloadNow()
                        }
                    }
                })

                var checkInterval = 0
                checkInterval = window.setInterval(
                    handler = {
                        window.fetch("${'$'}{window.location.origin}/api/kobweb/status").then {
                            it.text().then { text ->
                                if (lastStatus != text) {
                                    lastStatus = text
                                    if (text.isNotBlank()) {
                                        statusText.innerHTML = "<i>${'$'}text</i>"
                                        status.className = "fade-in"
                                    }
                                    else {
                                        status.className = "fade-out"
                                    }
                                }
                            }
                        }.catch {
                            // The server was probably taken down, so stop checking.
                            window.clearInterval(checkInterval)
                        }
                        window.fetch("${'$'}{window.location.origin}/api/kobweb/version").then {
                            it.text().then { text ->
                                val version = text.toInt()
                                if (lastVersion == null) {
                                    lastVersion = version
                                }
                                if (lastVersion != version) {
                                    lastVersion = version
                                    if (status.hasClass("fade-out")) {
                                        shouldReload = true
                                    } else {
                                        forceReloadNow()
                                    }
                                }
                            }
                        }.catch {
                            // The server was probably taken down, so stop checking.
                            window.clearInterval(checkInterval)
                        }
                    },
                    timeout = 250,
                )
            }
        }

        fun main() {
            ${if (target == BuildTarget.DEBUG) "pollServerStatus()" else "" }

            ${
                // Generates lines like: Router.register("/about") { AboutPage() }
                pageFqcnRoutes.entries.joinToString("\n            ") { entry ->
                    val pageFqcn = entry.key
                    val route = entry.value

                    """Router.register("$route") { $pageFqcn() }"""
                }
            }
            Router.navigateTo(window.location.pathname)

            // For SEO, we may bake the contents of a page in at build time. However, we will overwrite them the first
            // time we render this page with their composable, dynamic versions. Think of this as poor man's
            // hydration :) See also: https://en.wikipedia.org/wiki/Hydration_(web_development)
            val root = document.getElementById("root")!!
            while (root.firstChild != null) {
                root.removeChild(root.firstChild!!)
            }

            renderComposable(rootElementId = "root") {
                ${appFqcn?.let { appFqcn.substringAfterLast('.') } ?: "DefaultApp"} {
                    Router.renderActivePage()
                }
            }
        }
        """.trimIndent()
}