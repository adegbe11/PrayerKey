-- PrayerKey's private Supabase graph. Additive: does not alter existing Prisma tables.
-- Apply to the confirmed Supabase project only after reviewing and running the RLS tests.
begin;
create table public.pk_preferences (
 user_id uuid primary key references auth.users(id) on delete cascade,
 goals text[] not null default '{}',
 topics text[] not null default '{}',
 reminder_enabled boolean not null default false,
 reminder_hour smallint not null default 7 check (reminder_hour between 0 and 23),
 reminder_minute smallint not null default 0 check (reminder_minute between 0 and 59),
 timezone text not null default 'UTC',
 updated_at timestamptz not null default now()
);
create table public.pk_matters (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 title text not null check (length(title) between 1 and 300),
 intention text not null check (length(intention) between 1 and 10000),
 topics text[] not null default '{}',
 stage text not null default 'praying' check (stage in ('praying','waiting','acting','released','answered','answered_differently')),
 next_action text not null default '',
 created_at timestamptz not null default now(),
 updated_at timestamptz not null default now(),
 version bigint not null default 1,
 unique (user_id, id)
);
-- Canonical Scripture is imported from the bundled KJV; clients cannot write it.
create table public.pk_scriptures (
 reference text primary key,
 translation text not null default 'KJV' check (translation = 'KJV'),
 book text not null, chapter integer not null check (chapter > 0),
 verse integer not null check (verse > 0), text text not null check (length(text) > 0),
 unique(book, chapter, verse)
);
create table public.pk_people (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 name text not null check (length(name) between 1 and 200), relationship text not null default '',
 created_at timestamptz not null default now(),
 unique(user_id,id)
);
create table public.pk_sermons (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 title text not null check (length(title) between 1 and 300), notes text not null default '', preached_at timestamptz,
 created_at timestamptz not null default now(),
 unique(user_id,id)
);
create table public.pk_routines (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 kind text not null check (kind in ('challenge','fast')), title text not null check (length(title) between 1 and 300), starts_on date not null, ends_on date not null, status text not null default 'planned' check (status in ('planned','active','completed','stopped')), check (ends_on >= starts_on),
 created_at timestamptz not null default now(),
 unique(user_id,id)
);
create table public.pk_journal_entries (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 matter_id uuid,
 body text not null check (length(body) between 1 and 30000),
 created_at timestamptz not null default now(),
 unique(user_id,id),
 foreign key (user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade
);
create table public.pk_sessions (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 matter_id uuid not null,
 prayer text not null check (length(prayer) between 1 and 30000),
 source text not null check (source in ('pray','scripture','journal','sermon','morning','evening','challenge','fast')),
 completed_at timestamptz,
 created_at timestamptz not null default now(),
 unique(user_id,id),
 foreign key (user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade
);
create table public.pk_prayer_points (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 matter_id uuid not null,
 body text not null check (length(body) between 1 and 3000),
 created_at timestamptz not null default now(),
 foreign key (user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade
);
create table public.pk_updates (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade default auth.uid(),
 matter_id uuid not null,
 kind text not null check (kind in ('reflection','action','answer','testimony')),
 body text not null check (length(body) between 1 and 10000),
 created_at timestamptz not null default now(),
 foreign key (user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade
);
create table public.pk_matter_scriptures (
 user_id uuid not null default auth.uid(),
 matter_id uuid not null,
 reference text not null references public.pk_scriptures(reference),
 created_at timestamptz not null default now(),
 primary key (user_id,matter_id,reference),
 foreign key (user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade
);
create table public.pk_matter_people (
 user_id uuid not null default auth.uid(), matter_id uuid not null, target_id uuid not null,
 created_at timestamptz not null default now(),
 primary key(user_id,matter_id,target_id),
 foreign key(user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade,
 foreign key(user_id,target_id) references public.pk_people(user_id,id) on delete cascade
);
create table public.pk_matter_sermons (
 user_id uuid not null default auth.uid(), matter_id uuid not null, target_id uuid not null,
 created_at timestamptz not null default now(),
 primary key(user_id,matter_id,target_id),
 foreign key(user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade,
 foreign key(user_id,target_id) references public.pk_sermons(user_id,id) on delete cascade
);
create table public.pk_matter_routines (
 user_id uuid not null default auth.uid(), matter_id uuid not null, target_id uuid not null,
 created_at timestamptz not null default now(),
 primary key(user_id,matter_id,target_id),
 foreign key(user_id,matter_id) references public.pk_matters(user_id,id) on delete cascade,
 foreign key(user_id,target_id) references public.pk_routines(user_id,id) on delete cascade
);

alter table public.pk_preferences enable row level security;
revoke all on public.pk_preferences from anon, authenticated;
grant select, insert, update, delete on public.pk_preferences to authenticated;
create policy owner_only on public.pk_preferences for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);


alter table public.pk_matters enable row level security;
revoke all on public.pk_matters from anon, authenticated;
grant select, insert, update, delete on public.pk_matters to authenticated;
create policy owner_only on public.pk_matters for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_matters_owner on public.pk_matters(user_id);

alter table public.pk_people enable row level security;
revoke all on public.pk_people from anon, authenticated;
grant select, insert, update, delete on public.pk_people to authenticated;
create policy owner_only on public.pk_people for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_people_owner on public.pk_people(user_id);

alter table public.pk_sermons enable row level security;
revoke all on public.pk_sermons from anon, authenticated;
grant select, insert, update, delete on public.pk_sermons to authenticated;
create policy owner_only on public.pk_sermons for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_sermons_owner on public.pk_sermons(user_id);

alter table public.pk_routines enable row level security;
revoke all on public.pk_routines from anon, authenticated;
grant select, insert, update, delete on public.pk_routines to authenticated;
create policy owner_only on public.pk_routines for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_routines_owner on public.pk_routines(user_id);

alter table public.pk_journal_entries enable row level security;
revoke all on public.pk_journal_entries from anon, authenticated;
grant select, insert, update, delete on public.pk_journal_entries to authenticated;
create policy owner_only on public.pk_journal_entries for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_journal_entries_owner on public.pk_journal_entries(user_id);

alter table public.pk_sessions enable row level security;
revoke all on public.pk_sessions from anon, authenticated;
grant select, insert, update, delete on public.pk_sessions to authenticated;
create policy owner_only on public.pk_sessions for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_sessions_owner on public.pk_sessions(user_id);

alter table public.pk_prayer_points enable row level security;
revoke all on public.pk_prayer_points from anon, authenticated;
grant select, insert, update, delete on public.pk_prayer_points to authenticated;
create policy owner_only on public.pk_prayer_points for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_prayer_points_owner on public.pk_prayer_points(user_id);

alter table public.pk_updates enable row level security;
revoke all on public.pk_updates from anon, authenticated;
grant select, insert, update, delete on public.pk_updates to authenticated;
create policy owner_only on public.pk_updates for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_updates_owner on public.pk_updates(user_id);

alter table public.pk_matter_scriptures enable row level security;
revoke all on public.pk_matter_scriptures from anon, authenticated;
grant select, insert, update, delete on public.pk_matter_scriptures to authenticated;
create policy owner_only on public.pk_matter_scriptures for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_matter_scriptures_owner on public.pk_matter_scriptures(user_id);

alter table public.pk_matter_people enable row level security;
revoke all on public.pk_matter_people from anon, authenticated;
grant select, insert, update, delete on public.pk_matter_people to authenticated;
create policy owner_only on public.pk_matter_people for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_matter_people_owner on public.pk_matter_people(user_id);

alter table public.pk_matter_sermons enable row level security;
revoke all on public.pk_matter_sermons from anon, authenticated;
grant select, insert, update, delete on public.pk_matter_sermons to authenticated;
create policy owner_only on public.pk_matter_sermons for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_matter_sermons_owner on public.pk_matter_sermons(user_id);

alter table public.pk_matter_routines enable row level security;
revoke all on public.pk_matter_routines from anon, authenticated;
grant select, insert, update, delete on public.pk_matter_routines to authenticated;
create policy owner_only on public.pk_matter_routines for all to authenticated
 using ((select auth.uid()) = user_id)
 with check ((select auth.uid()) = user_id);
create index pk_matter_routines_owner on public.pk_matter_routines(user_id);

alter table public.pk_scriptures enable row level security;
revoke all on public.pk_scriptures from anon, authenticated;
grant select on public.pk_scriptures to authenticated;
create policy authenticated_read on public.pk_scriptures for select to authenticated using (true);
create index pk_sessions_matter_date on public.pk_sessions(user_id,matter_id,created_at desc);
create index pk_updates_matter_date on public.pk_updates(user_id,matter_id,created_at desc);
create index pk_journal_matter_date on public.pk_journal_entries(user_id,matter_id,created_at desc);
create function public.pk_touch_matter() returns trigger language plpgsql set search_path = '' as $$
begin
 new.updated_at = now();
 new.version = old.version + 1;
 return new;
end;
$$;
create trigger pk_matter_revision before update on public.pk_matters
 for each row execute function public.pk_touch_matter();
-- Security invoker: RLS still applies. Record outcome + its testimony atomically.
create function public.pk_record_outcome(p_matter_id uuid, p_version bigint, p_stage text, p_testimony text)
returns public.pk_matters language plpgsql security invoker set search_path = '' as $$
declare result public.pk_matters;
begin
 if p_stage not in ('answered','answered_differently') or p_stage is null then
  raise exception 'Invalid outcome' using errcode = '22023';
 end if;
 if p_testimony is null or length(trim(p_testimony)) not between 1 and 10000 then
  raise exception 'A recorded account is required' using errcode = '22023';
 end if;
 update public.pk_matters set stage = p_stage
 where id = p_matter_id and user_id = (select auth.uid()) and version = p_version
 returning * into result;
 if not found then raise exception 'Matter unavailable or changed; reload before saving' using errcode = '40001'; end if;
 insert into public.pk_updates(user_id,matter_id,kind,body)
 values ((select auth.uid()),p_matter_id,'testimony',trim(p_testimony));
 return result;
end;
$$;
revoke all on function public.pk_record_outcome(uuid,bigint,text,text) from public, anon;
grant execute on function public.pk_record_outcome(uuid,bigint,text,text) to authenticated;
commit;

