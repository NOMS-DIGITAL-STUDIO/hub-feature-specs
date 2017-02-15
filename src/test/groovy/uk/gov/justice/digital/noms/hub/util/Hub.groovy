package uk.gov.justice.digital.noms.hub.util

import groovy.util.logging.Slf4j

@Slf4j
class Hub {
    String adminUri
    String adminUiUri
    String username
    String password

    Hub() {
        username = System.getenv('BASIC_AUTH_USERNAME') ?: 'user'
        password = System.getenv('BASIC_AUTH_PASSWORD') ?: 'password'

        adminUri = (System.getenv('HUB_ADMIN_URI') ?: "http://localhost:8080/")
        log.info("adminUri: ${adminUri}")
        adminUri = adminUri.replaceFirst('^https?://', "http://${username}:${password}@")

        adminUiUri = (System.getenv('HUB_ADMIN_UI_URI') ?: "http://localhost:3000/")
        log.info("adminUiUri: ${adminUiUri}")
        adminUiUri = adminUiUri.replaceFirst('^https?://', "http://${username}:${password}@")
    }

}
