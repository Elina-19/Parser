create table item
(
    id             serial primary key,
    product_id     varchar,
    title          varchar,
    min_full_price int,
    min_sell_price int,
    rating         float
);
