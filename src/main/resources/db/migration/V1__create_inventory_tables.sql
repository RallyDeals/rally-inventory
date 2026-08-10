CREATE TABLE inventory (
       product_id UUID PRIMARY KEY,

       total_stock INTEGER NOT NULL,
       reserved_stock INTEGER NOT NULL,
       available_stock INTEGER NOT NULL,

       version BIGINT NOT NULL DEFAULT 0,

       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT chk_total_stock CHECK (total_stock >= 0),
       CONSTRAINT chk_reserved_stock CHECK (reserved_stock >= 0),
       CONSTRAINT chk_available_stock CHECK (available_stock >= 0)
);

CREATE TABLE inventory_history (
       id BIGSERIAL PRIMARY KEY,

       product_id UUID NOT NULL,

       operation_type VARCHAR(50) NOT NULL,

       quantity INTEGER NOT NULL,

       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT chk_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_inventory_history_product_id
    ON inventory_history(product_id);