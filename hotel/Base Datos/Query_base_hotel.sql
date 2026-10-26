IF DB_ID('hotel_db') IS NULL
    CREATE DATABASE hotel_db;
GO

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'hotel_user')
    CREATE LOGIN hotel_user WITH PASSWORD = 'Hotel123*', CHECK_POLICY = OFF;
GO

USE hotel_db;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'hotel_user')
    CREATE USER hotel_user FOR LOGIN hotel_user;
ALTER ROLE db_owner ADD MEMBER hotel_user;
GO