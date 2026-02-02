FROM quay.io/keycloak/keycloak:25.0.2
COPY realm-communityhub.json /opt/keycloak/data/import/realm-communityhub.json
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]
