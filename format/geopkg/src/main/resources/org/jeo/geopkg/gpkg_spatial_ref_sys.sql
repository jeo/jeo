CREATE TABLE IF NOT EXISTS gpkg_spatial_ref_sys ( 
  srs_name TEXT NOT NULL, 
  srs_id INTEGER NOT NULL PRIMARY KEY, 
  organization TEXT NOT NULL, 
  organization_coordsys_id INTEGER NOT NULL, 
  definition  TEXT NOT NULL, 
  description TEXT );
CREATE UNIQUE INDEX IF NOT EXISTS gpkg_spatial_ref_sys_idx ON gpkg_spatial_ref_sys (srs_id, organization);
INSERT OR IGNORE INTO gpkg_spatial_ref_sys VALUES ('Undefined cartesian SRS',-1,'NONE',-1,'undefined','undefined cartesian coordinate reference system');
INSERT OR IGNORE INTO gpkg_spatial_ref_sys VALUES ('Undefined cartesian SRS',0,'NONE',0,'undefined','undefined cartesian coordinate reference system');

