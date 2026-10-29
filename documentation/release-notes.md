# Release notes

Overzicht van wijzigingen per versie van de Hasura-plugin.

## 1.5.0

- De Hasura-plugin authenticeert nu bij Hasura via een Keycloak client-credentials grant
  (nieuwe pluginproperties `tokenUrl`, `clientId` en `clientSecret`) in plaats van een vast
  `hasuraAdminSecret`. Het opgehaalde token wordt gecachet en automatisch ververst; bij een
  ongeldig of verlopen token wordt de aanvraag één keer automatisch opnieuw geprobeerd met een
  vers token.
- Het resultaat van zowel de "GraphQL by Input"- als de "Mutation by Process Variable"-actie
  wordt nu opgeslagen als één object met de velden `statusCode` en `result`. Een GraphQL-fout
  van Hasura (bijvoorbeeld een permissiefout) laat de service-task falen met de GraphQL-foutmelding,
  in plaats van een leeg of foutief resultaat stilletjes in de procesvariabele op te slaan.

## 1.3.2

Ondersteuning voor Valtimo 13.48.0.

## 1.3.1

Valtimo bijgewerkt naar versie 13.41.0.
