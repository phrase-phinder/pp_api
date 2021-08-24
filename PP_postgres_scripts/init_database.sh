#!/bin/bash
export PGPASSWORD=postgres
psql -U postgres -h $POSTGRES_IP -d postgres -f PP_create_database.sql
psql -U postgres -h $POSTGRES_IP -d phrasephinder -f PP_create_schema.sql

