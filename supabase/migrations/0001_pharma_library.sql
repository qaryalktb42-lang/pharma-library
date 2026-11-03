-- PHARMA LIBRARY schema
-- This migration is prepared for deployment to the user's Supabase project.
-- It contains no credentials or service_role secrets.

create extension if not exists pgcrypto;

do $$ begin
  create type public.app_role as enum ('owner', 'supervisor');
exception when duplicate_object then null; end $$;

do $$ begin
  create type public.asset_type as enum ('pdf', 'txt', 'docx', 'image', 'audio', 'video', 'summary', 'questions', 'translation', 'reference');
exception when duplicate_object then null; end $$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  role public.app_role not null default 'supervisor',
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.study_years (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  sort_order integer not null default 0,
  is_visible boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.semesters (
  id uuid primary key default gen_random_uuid(),
  study_year_id uuid not null references public.study_years(id) on delete cascade,
  title text not null,
  description text,
  sort_order integer not null default 0,
  is_visible boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.subjects (
  id uuid primary key default gen_random_uuid(),
  semester_id uuid not null references public.semesters(id) on delete cascade,
  title text not null,
  description text,
  lecturer text,
  sort_order integer not null default 0,
  is_visible boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.academic_batches (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  is_visible boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.subject_batches (
  subject_id uuid not null references public.subjects(id) on delete cascade,
  batch_id uuid not null references public.academic_batches(id) on delete cascade,
  primary key (subject_id, batch_id)
);

create table if not exists public.lectures (
  id uuid primary key default gen_random_uuid(),
  subject_id uuid not null references public.subjects(id) on delete cascade,
  batch_id uuid not null references public.academic_batches(id) on delete cascade,
  lecture_number integer not null,
  title text not null,
  description text,
  lecturer text,
  cover_path text,
  is_visible boolean not null default true,
  created_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(subject_id, batch_id, lecture_number)
);

create table if not exists public.content_sections (
  id uuid primary key default gen_random_uuid(),
  lecture_id uuid not null references public.lectures(id) on delete cascade,
  title text not null,
  sort_order integer not null default 0,
  is_visible boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.file_assets (
  id uuid primary key default gen_random_uuid(),
  section_id uuid not null references public.content_sections(id) on delete cascade,
  name text not null,
  description text,
  asset_type public.asset_type not null,
  storage_path text not null unique,
  size_bytes bigint,
  mime_type text,
  sort_order integer not null default 0,
  is_visible boolean not null default true,
  uploaded_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.supervisor_permissions (
  user_id uuid not null references public.profiles(id) on delete cascade,
  subject_id uuid not null references public.subjects(id) on delete cascade,
  can_edit boolean not null default true,
  can_delete boolean not null default false,
  created_at timestamptz not null default now(),
  primary key (user_id, subject_id)
);

create table if not exists public.announcements (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  is_visible boolean not null default true,
  published_at timestamptz not null default now(),
  created_by uuid references auth.users(id) on delete set null
);

create table if not exists public.activity_logs (
  id uuid primary key default gen_random_uuid(),
  actor_id uuid references auth.users(id) on delete set null,
  action text not null,
  entity_type text not null,
  entity_id uuid,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create table if not exists public.app_settings (
  key text primary key,
  value jsonb not null default '{}'::jsonb,
  updated_by uuid references auth.users(id) on delete set null,
  updated_at timestamptz not null default now()
);

create index if not exists semesters_year_idx on public.semesters(study_year_id, sort_order);
create index if not exists subjects_semester_idx on public.subjects(semester_id, sort_order);
create index if not exists lectures_subject_batch_idx on public.lectures(subject_id, batch_id, lecture_number);
create index if not exists assets_section_idx on public.file_assets(section_id, sort_order);
create index if not exists permissions_subject_idx on public.supervisor_permissions(subject_id);

create or replace function public.current_role()
returns public.app_role
language sql stable security definer set search_path = public
as $$ select role from public.profiles where id = auth.uid() and is_active = true limit 1 $$;

create or replace function public.is_owner()
returns boolean
language sql stable security definer set search_path = public
as $$ select public.current_role() = 'owner'::public.app_role $$;

create or replace function public.can_manage_subject(target_subject uuid)
returns boolean
language sql stable security definer set search_path = public
as $$
  select public.is_owner() or exists (
    select 1 from public.supervisor_permissions
    where user_id = auth.uid() and subject_id = target_subject and can_edit = true
  )
$$;

alter table public.profiles enable row level security;
alter table public.study_years enable row level security;
alter table public.semesters enable row level security;
alter table public.subjects enable row level security;
alter table public.academic_batches enable row level security;
alter table public.subject_batches enable row level security;
alter table public.lectures enable row level security;
alter table public.content_sections enable row level security;
alter table public.file_assets enable row level security;
alter table public.supervisor_permissions enable row level security;
alter table public.announcements enable row level security;
alter table public.activity_logs enable row level security;
alter table public.app_settings enable row level security;

-- Public students can read only visible academic/content rows.
create policy "public reads visible years" on public.study_years for select using (is_visible);
create policy "public reads visible semesters" on public.semesters for select using (is_visible);
create policy "public reads visible subjects" on public.subjects for select using (is_visible);
create policy "public reads visible batches" on public.academic_batches for select using (is_visible);
create policy "public reads subject batches" on public.subject_batches for select using (true);
create policy "public reads visible lectures" on public.lectures for select using (is_visible);
create policy "public reads visible sections" on public.content_sections for select using (is_visible);
create policy "public reads visible assets" on public.file_assets for select using (is_visible);
create policy "public reads announcements" on public.announcements for select using (is_visible);

-- Owners can manage everything; supervisors are restricted by subject permission.
create policy "owners manage profiles" on public.profiles for all using (public.is_owner() or id = auth.uid()) with check (public.is_owner() or id = auth.uid());
create policy "owners manage years" on public.study_years for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage semesters" on public.semesters for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage subjects" on public.subjects for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage batches" on public.academic_batches for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage subject batches" on public.subject_batches for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage permissions" on public.supervisor_permissions for all using (public.is_owner()) with check (public.is_owner());
create policy "owners manage settings" on public.app_settings for all using (public.is_owner()) with check (public.is_owner());
create policy "admins manage lectures" on public.lectures for insert with check (public.can_manage_subject(subject_id));
create policy "admins update lectures" on public.lectures for update using (public.can_manage_subject(subject_id)) with check (public.can_manage_subject(subject_id));
create policy "admins delete lectures" on public.lectures for delete using (public.is_owner() or exists (select 1 from public.supervisor_permissions p where p.user_id = auth.uid() and p.subject_id = lectures.subject_id and p.can_delete));
create policy "admins manage sections" on public.content_sections for all using (public.is_owner() or exists (select 1 from public.lectures l where l.id = lecture_id and public.can_manage_subject(l.subject_id))) with check (public.is_owner() or exists (select 1 from public.lectures l where l.id = lecture_id and public.can_manage_subject(l.subject_id)));
create policy "admins manage assets" on public.file_assets for all using (public.is_owner() or exists (select 1 from public.content_sections s join public.lectures l on l.id = s.lecture_id where s.id = section_id and public.can_manage_subject(l.subject_id))) with check (public.is_owner() or exists (select 1 from public.content_sections s join public.lectures l on l.id = s.lecture_id where s.id = section_id and public.can_manage_subject(l.subject_id)));
create policy "owners manage announcements" on public.announcements for all using (public.is_owner()) with check (public.is_owner());
create policy "owners read logs" on public.activity_logs for select using (public.is_owner());
create policy "admins insert logs" on public.activity_logs for insert with check (auth.uid() = actor_id);

insert into storage.buckets (id, name, public) values ('pharma-library-files', 'pharma-library-files', false) on conflict (id) do nothing;

-- Keep the bucket private. Student access will use signed URLs from a controlled API/Edge Function.
create policy "owners read private storage objects" on storage.objects for select using (bucket_id = 'pharma-library-files' and public.is_owner());
create policy "owners upload private storage objects" on storage.objects for insert with check (bucket_id = 'pharma-library-files' and public.is_owner());
create policy "owners delete storage objects" on storage.objects for delete using (bucket_id = 'pharma-library-files' and public.is_owner());
