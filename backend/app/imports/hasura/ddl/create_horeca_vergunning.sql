CREATE TABLE IF NOT EXISTS horeca_vergunning (
    id                                      INTEGER     PRIMARY KEY,
    zaaknummer                              INTEGER,
    zaaknaam                                TEXT,
    adres                                   TEXT,
    zaak_categorie                          TEXT,
    zaak_specificatie                       TEXT,
    begindatum                              DATE,
    einddatum                              DATE,

    -- Openingstijden zondag t/m donderdag
    openingstijden_zo_do_van                TEXT,
    openingstijden_zo_do_tot                TEXT,

    -- Openingstijden vrijdag en zaterdag
    openingstijden_vr_za_van                TEXT,
    openingstijden_vr_za_tot                TEXT,

    -- Terras openingstijden zondag t/m donderdag
    o_tijden_terras_zo_do_van               TEXT,
    o_tijden_terras_zo_do_tot               TEXT,

    -- Terras openingstijden vrijdag en zaterdag
    o_tijden_terras_vr_za_van               TEXT,
    o_tijden_terras_vr_za_tot               TEXT,

    -- Geometrie
    locatie                                 TEXT,
    terrasgeometrie                         TEXT,

    postcode                                TEXT,

    -- Vergunning status
    status_vergunning                       TEXT,

    -- Tijdelijk terras
    status_tijdelijk_terras                 TEXT,
    toestemming_tijdelijk_terras            TEXT,
    publ_besluit_tijdelijk_terras           TEXT,
    tijdelijk_terras_details                TEXT,

    -- Verlenging tijdelijk terras
    status_verlenging_tijdelijk_terras      TEXT,
    verlenging_tijdelijk_terras_details     TEXT,

    updated_at                              TIMESTAMPTZ NOT NULL
);
