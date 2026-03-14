-- Seed one demo API key per account so users can authenticate immediately.
-- These are for development/demo only. In production, keys would be provisioned
-- through a secure onboarding flow.
--
-- Full demo keys (use these in the Authorization header as "Bearer <key>"):
--   Alice: pp_live_AliceDemoKey0000000000000000000000000000
--   Bob:   pp_live_BobDemoKey000000000000000000000000000000
--   Carol: pp_live_CarolDemoKey0000000000000000000000000000
--   Dave:  pp_live_DaveDemoKey00000000000000000000000000000

INSERT INTO api_key (key_hash, name, account_id, active, created_at) VALUES
  ('2a6921568c836035a1de30ac773ec26ae573d9509ab1bac2597f3b9010ebd9d7', 'alice-demo-key', 1, true, NOW()),
  ('a105128f0e9ed7f25d9ec88939edffa2a071439840eb5d86659e65ef5d37a890', 'bob-demo-key',   2, true, NOW()),
  ('47d788be6abb8c185c250522f4561ec3e5b7baaed2e264111687dd4ea565f8e3', 'carol-demo-key', 3, true, NOW()),
  ('8a069083c2dade5118bea3732d48c91d19c90e293c1ffd44aa54a9863dfa55db', 'dave-demo-key',  4, true, NOW());