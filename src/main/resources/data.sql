-- create donor table
DROP
    TABLE
        IF EXISTS tbl_donors CASCADE;

CREATE
    TABLE
        tbl_donors(
            user_id UUID NOT NULL PRIMARY KEY,
            last_name VARCHAR(255) NOT NULL ,
            first_name VARCHAR(255) NOT NULL ,
            address VARCHAR(255),
            stripe_id VARCHAR(255),
            assets_key VARCHAR(255)
        );

-- Drop organization_types table if it exists
DROP
    TABLE
        IF EXISTS organization_types CASCADE;

-- Create organization_types table
CREATE
    TABLE organization_types (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL UNIQUE
    );

-- Populate organization_types with initial values
INSERT
    INTO organization_types (name) VALUES
        ('COMPANY'),
        ('INDIVIDUAL'),
        ('NON_PROFIT');

-- create charities table
DROP
    TABLE
        IF EXISTS tbl_charities CASCADE;

CREATE
    TABLE
        tbl_charities(
            user_id UUID NOT NULL PRIMARY KEY,
            company_name VARCHAR(255) NOT NULL ,
            address VARCHAR(255) NOT NULL ,
            tax_code VARCHAR(255),
            stripe_id VARCHAR(255),
            assets_key VARCHAR(255),
            organization_type VARCHAR(50) NOT NULL
);

-- Add a constraint to ensure only valid organization types are allowed
ALTER TABLE tbl_charities
    ADD CONSTRAINT chk_organization_type
        CHECK (organization_type IN ('COMPANY', 'INDIVIDUAL', 'NON_PROFIT'));

-- spring monolith table
DROP
    TABLE
        IF EXISTS event_publication CASCADE;

CREATE
    TABLE
        event_publication(
            completion_date TIMESTAMP(6) WITH TIME ZONE,
            publication_date TIMESTAMP(6) WITH TIME ZONE,
            id uuid NOT NULL PRIMARY KEY,
            event_type VARCHAR(255),
            listener_id VARCHAR(255),
            serialized_event VARCHAR(255)
        );

-- init user
INSERT
    INTO
        public.tbl_donors(
            user_id,
            last_name,
            first_name,
            address,
            stripe_id,
            assets_key
        )
    VALUES(
        'a55313de-32be-4b20-867b-b7d07042e629',
        'Do',
        'Nguyen',
        'Rmit Uni',
        'cus_RZVBbWpAaZq8Lc',
        '/a55313de-32be-4b20-867b-b7d07042e629'
    ),
    (
        'e99c1730-4f6c-4022-95de-a5487351a938',
        'Trung',
        'Le',
        'Ho Chi Minh',
        'cus_RZVBIs8LffImss',
        '/e99c1730-4f6c-4022-95de-a5487351a938'
      ) ON
    CONFLICT DO NOTHING;

INSERT
    INTO
        public.tbl_charities(
            user_id,
            company_name,
            address,
            tax_code,
            stripe_id,
            assets_key,
            organization_type
        )
VALUES(
        'e69accdd-4121-4172-a072-bf181a21cbfd',
        'Binh Corp',
        'e69accdd-4121-4172-a072-bf181a21cbfd',
        'BC123',
        'cus_RZVBlvuyzkdfzo',
        '/e69accdd-4121-4172-a072-bf181a21cbfd',
        'INDIVIDUAL'
      ) ON
    CONFLICT DO NOTHING;
