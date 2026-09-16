begin;
create function public.pk_save_preferences(p_preferences jsonb) returns public.pk_preferences
language plpgsql security invoker set search_path = '' as $$
declare result public.pk_preferences;
begin
 insert into public.pk_preferences(user_id,goals,topics,reminder_enabled,reminder_hour,reminder_minute,timezone)
 values ((select auth.uid()),
  array(select jsonb_array_elements_text(p_preferences->'goals')),
  array(select jsonb_array_elements_text(p_preferences->'topics')),
  (p_preferences->>'reminder_enabled')::boolean,
  (p_preferences->>'reminder_hour')::smallint,
  (p_preferences->>'reminder_minute')::smallint,
  p_preferences->>'timezone')
 on conflict(user_id) do update set goals = excluded.goals, topics = excluded.topics,
  reminder_enabled = excluded.reminder_enabled, reminder_hour = excluded.reminder_hour,
  reminder_minute = excluded.reminder_minute, timezone = excluded.timezone, updated_at = now()
 returning * into result;
 return result;
end;
$$;
revoke all on function public.pk_save_preferences(jsonb) from public,anon;
grant execute on function public.pk_save_preferences(jsonb) to authenticated;
commit;
