-- V2 Migration: Add optional nutrition info to recipes and expiry/low-stock to pantry items

-- V2 Migration: Add optional nutrition info to recipes and expiry/low-stock to pantry items

ALTER TABLE recipes ADD COLUMN calories INT NULL;
ALTER TABLE recipes ADD COLUMN protein_grams DECIMAL(6,1) NULL;
ALTER TABLE recipes ADD COLUMN carbs_grams DECIMAL(6,1) NULL;
ALTER TABLE recipes ADD COLUMN fat_grams DECIMAL(6,1) NULL;

ALTER TABLE pantry_items ADD COLUMN expiry_date DATE NULL;
ALTER TABLE pantry_items ADD COLUMN low_stock_threshold DECIMAL(10,3) NULL;

