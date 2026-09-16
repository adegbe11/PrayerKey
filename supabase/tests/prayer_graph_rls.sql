-- Run with psql -v ON_ERROR_STOP=1 on a LOCAL/test Supabase database after migrations.
-- Every fixture is rolled back. This is an execution test, not a production seed.
begin;
select set_config('pk.test_user_a', gen_random_uuid()::text, true);
select set_config('pk.test_user_b', gen_random_uuid()::text, true);
select set_config('pk.test_matter', gen_random_uuid()::text, true);
select set_config('pk.test_person', gen_random_uuid()::text, true);
insert into auth.users(id) values (current_setting('pk.test_user_a')::uuid), (current_setting('pk.test_user_b')::uuid);
set local role authenticated;
select set_config('request.jwt.claim.sub', current_setting('pk.test_user_a'), true);
insert into public.pk_matters(id,user_id,title,intention) values
 (current_setting('pk.test_matter')::uuid,auth.uid(),'My daughter','University admission');
insert into public.pk_people(id,user_id,name) values (current_setting('pk.test_person')::uuid,auth.uid(),'My daughter');
insert into public.pk_matter_people(user_id,matter_id,target_id) values
 (auth.uid(),current_setting('pk.test_matter')::uuid,current_setting('pk.test_person')::uuid);
do $$ begin
 if (select count(*) from public.pk_matters) <> 1 then raise exception 'Owner cannot see matter'; end if;
 begin
  insert into public.pk_matter_people(user_id,matter_id,target_id) values
   (auth.uid(),current_setting('pk.test_matter')::uuid,current_setting('pk.test_person')::uuid);
  raise exception 'Duplicate graph edge accepted';
 exception when unique_violation then null; end;
end $$;
select set_config('request.jwt.claim.sub', current_setting('pk.test_user_b'), true);
do $$ begin
 if exists(select 1 from public.pk_matters) then raise exception 'Cross-user read allowed'; end if;
 update public.pk_matters set title = 'Intrusion' where id = current_setting('pk.test_matter')::uuid;
 if found then raise exception 'Cross-user update allowed'; end if;
 begin
  insert into public.pk_matters(user_id,title,intention) values(current_setting('pk.test_user_a')::uuid,'Forged','Owner');
  raise exception 'Forged owner accepted';
 exception when insufficient_privilege then null; end;
 begin
  insert into public.pk_matter_people(user_id,matter_id,target_id) values
   (auth.uid(),current_setting('pk.test_matter')::uuid,current_setting('pk.test_person')::uuid);
  raise exception 'Cross-user edge accepted';
 exception when foreign_key_violation then null; end;
end $$;
select set_config('request.jwt.claim.sub', current_setting('pk.test_user_a'), true);
select public.pk_record_outcome(current_setting('pk.test_matter')::uuid,1,'answered','She received her admission letter.');
do $$ begin
 if (select count(*) from public.pk_updates where kind='testimony') <> 1 then raise exception 'Testimony missing'; end if;
 begin
  perform public.pk_record_outcome(current_setting('pk.test_matter')::uuid,1,'answered','Duplicate stale submission');
  raise exception 'Stale revision accepted';
 exception when serialization_failure then null; end;
 if (select count(*) from public.pk_updates) <> 1 then raise exception 'Duplicate testimony created'; end if;
 begin
  insert into public.pk_matter_scriptures(user_id,matter_id,reference) values(auth.uid(),current_setting('pk.test_matter')::uuid,'Imaginary 99:999');
  raise exception 'Fabricated Scripture accepted';
 exception when foreign_key_violation then null; end;
end $$;
reset role;
set local role anon;
do $$ begin
 begin
  perform * from public.pk_matters;
  raise exception 'Anonymous read allowed';
 exception when insufficient_privilege then null; end;
end $$;
reset role;
rollback;
