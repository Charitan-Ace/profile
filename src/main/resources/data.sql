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
      ),
      ('7819b4f1-8a94-4f8c-99bc-d90be981c719', 'An', 'Tran', 'Hanoi', 'cus_RZUgmyUcwqQ2Xj', '/7819b4f1-8a94-4f8c-99bc-d90be981c719'),
      ('cd83b706-6e08-4a3b-9433-7f49e19c8a57', 'Linh', 'Pham', 'Da Nang', 'cus_RZUgQLwlixpBVk', '/cd83b706-6e08-4a3b-9433-7f49e19c8a57'),
      ('2a708f74-3f64-45ab-9e17-92a953f9df5a', 'Duc', 'Vu', 'Hue', 'cus_RZUgB3mE32BlWb', '/2a708f74-3f64-45ab-9e17-92a953f9df5a'),
      ('b4fc1c09-5082-4a1d-a48c-6870a1c9b6f3', 'Hoa', 'Nguyen', 'Vung Tau', 'cus_RZUg6agwWDcGWD', '/b4fc1c09-5082-4a1d-a48c-6870a1c9b6f3'),
      ('8d3e4329-36d3-4c9e-a7cb-95750573a9bc', 'Minh', 'Tran', 'Nha Trang', 'cus_RZUgNoEWKemC8O', '/8d3e4329-36d3-4c9e-a7cb-95750573a9bc'),
      ('ba364d5e-fd36-4f97-9fc3-3d24884365df', 'Huy', 'Le', 'Can Tho', 'cus_RZU30qtUKp9kXh', '/ba364d5e-fd36-4f97-9fc3-3d24884365df'),
      ('f1a6c8b7-c81f-4a9e-b0f1-51e4788133d1', 'Thao', 'Nguyen', 'Quang Ninh', 'cus_RZU3EjkMaYVuRO', '/f1a6c8b7-c81f-4a9e-b0f1-51e4788133d1'),
      ('b59f8321-f0f7-4890-880f-70cb925e849e', 'Duyen', 'Pham', 'Hai Phong', 'cus_RZU3OXK0pr4YFX', '/b59f8321-f0f7-4890-880f-70cb925e849e'),
      ('a28f8326-8e1b-4235-89cb-798c4fe0b28c', 'Nam', 'Nguyen', 'Long An', 'cus_RZU3mbfG9ZQAzS', '/a28f8326-8e1b-4235-89cb-798c4fe0b28c'),
      ('799ab6f9-f2bb-4cc9-b9f8-5de7d3d82918', 'Khanh', 'Tran', 'An Giang', 'cus_RZU3DDtMtXryUw', '/799ab6f9-f2bb-4cc9-b9f8-5de7d3d82918'),
      ('fc31896b-b2cc-44e7-b9a3-6d21a457ec43', 'Tien', 'Le', 'Gia Lai', 'cus_RZU3RFA0X305i7', '/fc31896b-b2cc-44e7-b9a3-6d21a457ec43'),
      ('a6e7f10b-9d7f-4965-9c38-73376af7ad31', 'Phong', 'Nguyen', 'Bac Ninh', 'cus_RZU3LYfjGq6u57', '/a6e7f10b-9d7f-4965-9c38-73376af7ad31'),
      ('29d67b62-b7a2-4c0e-9bf2-d70d206ff4f2', 'Thuy', 'Pham', 'Binh Duong', 'cus_RZU3CZsJWAc6tR', '/29d67b62-b7a2-4c0e-9bf2-d70d206ff4f2'),
      ('fbc94628-dab8-435c-aeb3-87a679ed54c2', 'Quyen', 'Vu', 'Dong Nai', 'cus_RZU3I2J2ePOqzX', '/fbc94628-dab8-435c-aeb3-87a679ed54c2'),
      ('c68dc78a-fc36-4df6-96fa-bcb5f1a3c0e5', 'Hanh', 'Nguyen', 'Tien Giang', 'cus_RZU3pIuuedqTTB', '/c68dc78a-fc36-4df6-96fa-bcb5f1a3c0e5'),
      ('5fc4b2fa-693c-4a2e-851e-b1ae1e6c2ed9', 'Lan', 'Hoang', 'Lam Dong', 'cus_RZT7UTexIDkfI1', '/5fc4b2fa-693c-4a2e-851e-b1ae1e6c2ed9'),
      ('eb0fbe72-3b85-4e9b-973b-7e8b7cbf7491', 'Dinh', 'Nguyen', 'Son La', 'cus_RZT7kX0cdcPncN', '/eb0fbe72-3b85-4e9b-973b-7e8b7cbf7491'),
      ('84df7e63-0331-40b9-bdd6-3908b6c7ddcc', 'Vuong', 'Pham', 'Phu Yen', 'cus_RZT7GMdxRFAhCR', '/84df7e63-0331-40b9-bdd6-3908b6c7ddcc'),
      ('91e7ae78-8337-4bc6-b895-979cf5ef46a9', 'Hieu', 'Tran', 'Hai Duong', 'cus_RZT7qKqctaQY2U', '/91e7ae78-8337-4bc6-b895-979cf5ef46a9'),
      ('17b854ba-5c2f-49b6-a156-8c7c9f32f76f', 'Tam', 'Nguyen', 'Cao Bang', 'cus_RZT7KIq8QAnCJn', '/17b854ba-5c2f-49b6-a156-8c7c9f32f76f'),
      ('ccfbd6f5-8d35-4d89-b716-2b2c2dbfcd14', 'Hai', 'Le', 'Quang Tri', 'cus_RZT7zahOm0OLM9', '/ccfbd6f5-8d35-4d89-b716-2b2c2dbfcd14'),
      ('f53a2c7f-b7a3-41f3-a34f-e7c92e23c021', 'Binh', 'Vu', 'Kien Giang', 'cus_RZT7ySRxYZMsta', '/f53a2c7f-b7a3-41f3-a34f-e7c92e23c021'),
      ('9b30afc8-3d3a-4e37-8c43-d0f73ef19723', 'Luan', 'Nguyen', 'Ha Nam', 'cus_RZT7RJHCKIRoTG', '/9b30afc8-3d3a-4e37-8c43-d0f73ef19723'),
      ('013c30c7-0c76-41b2-b67e-12d5f3e9bfa7', 'Kieu', 'Pham', 'Thai Nguyen', 'cus_RZSbiYpZX5WYZO', '/013c30c7-0c76-41b2-b67e-12d5f3e9bfa7'),
      ('da362c75-03cd-4e83-b8a9-36d8c3342ecf', 'Hao', 'Nguyen', 'Nam Dinh', 'cus_RZSbKUCh9BdeDc', '/da362c75-03cd-4e83-b8a9-36d8c3342ecf'),
      ('4c312bc7-d25e-46cb-b193-7e81bfe20c9a', 'Chi', 'Tran', 'Thanh Hoa', 'cus_RZSb8saqn1FWVk', '/4c312bc7-d25e-46cb-b193-7e81bfe20c9a'),
      ('ac3126c9-4f96-44db-a9fb-97e7b32b6d8e', 'Thu', 'Hoang', 'Nghe An', 'cus_RZSbrttrpM4hrz', '/ac3126c9-4f96-44db-a9fb-97e7b32b6d8e'),
      ('663be12d-79b8-4c73-9b8f-5bb2b9e24618', 'Mau', 'Bach', 'Vung Tau', 'cus_RZQzdz0miqEGt4', '/663be12d-79b8-4c73-9b8f-5bb2b9e24618'),
    ON
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
VALUES
    ('e69accdd-4121-4172-a072-bf181a21cbfd', 'Binh Corp', 'Vietnam', 'BC123', 'cus_RZRdPydN4GbvcQ', '/e69accdd-4121-4172-a072-bf181a21cbfd', 'INDIVIDUAL'),
    ('9b75c19d-f70d-4c6d-b10b-3d517bbac1c8', 'USA Charity', 'USA', 'USA678', 'cus_RZRdgzYVwOAJp3', '/9b75c19d-f70d-4c6d-b10b-3d517bbac1c8', 'INDIVIDUAL'),
    ('6f9619ff-8b86-d011-b42d-00cf4fc964ff', 'South Africa Benz', 'South Africa', 'SA003', 'cus_RZRdYssJKc73mu', '/6f9619ff-8b86-d011-b42d-00cf4fc964ff', 'COMPANY'),
    ('d4e7aef8-629f-4419-91a7-8f3eb5bb5e2b', 'Germedic', 'Germany', 'GER111', 'cus_RZRdvcu21ihVap', '/d4e7aef8-629f-4419-91a7-8f3eb5bb5e2b', 'COMPANY'),
    ('72d4e2c7-85a7-4d90-8135-ef7418c39b1d', 'Ukraine Org', 'Ukraine', 'UKR333', 'cus_RZRdAbFTSyZF5h', '/72d4e2c7-85a7-4d90-8135-ef7418c39b1d', 'NON_PROFIT'),
    ('14eeb072-6635-45c3-aad5-7e76fda0b26e', 'Israel War', 'USA', 'USA678', 'cus_RZQzYjht0eD9m3', '/14eeb072-6635-45c3-aad5-7e76fda0b26e', 'NON_PROFIT')
    ON
    CONFLICT DO NOTHING;