-- Table: public.t_user

-- DROP TABLE public.t_user;

CREATE TABLE public.t_user
(
    user_id bigint NOT NULL,
    user_name character varying(10) COLLATE pg_catalog."default",
    login_name character varying(50) COLLATE pg_catalog."default",
    mobile character varying(11) COLLATE pg_catalog."default",
    email character varying(50) COLLATE pg_catalog."default",
    password character varying(200) COLLATE pg_catalog."default",
    salt character varying(64) COLLATE pg_catalog."default",
    status smallint,
    is_deleted smallint,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    remarks character varying(255) COLLATE pg_catalog."default",
    versions bigint DEFAULT 0,
    CONSTRAINT "`t_user_pkey" PRIMARY KEY (user_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_user
    OWNER to postgres;
COMMENT ON TABLE public.t_user
    IS '用户表';

COMMENT ON COLUMN public.t_user.user_name
    IS '真实姓名';

COMMENT ON COLUMN public.t_user.login_name
    IS '登录名';

COMMENT ON COLUMN public.t_user.mobile
    IS '手机号码';

COMMENT ON COLUMN public.t_user.email
    IS '邮箱';

COMMENT ON COLUMN public.t_user.salt
    IS '加密盐';

COMMENT ON COLUMN public.t_user.status
    IS '状态 0=未激活；1=激活；2=禁用；-1=删除';

-- Table: public.t_product_brand

-- DROP TABLE public.t_product_brand;

CREATE TABLE public.t_product_brand
(
    brand_id bigint NOT NULL,
    brand_name character varying(10) COLLATE pg_catalog."default",
    brand_sort integer,
    is_deleted smallint,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    remarks character varying(255) COLLATE pg_catalog."default",
    versions bigint DEFAULT 0,
    CONSTRAINT "`t_product_brand_pkey" PRIMARY KEY (brand_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_product_brand
    OWNER to postgres;
COMMENT ON TABLE public.t_product_brand
    IS '商品类别表';

COMMENT ON COLUMN public.t_product_brand.brand_name
    IS '商品类别名称';

COMMENT ON COLUMN public.t_product_brand.brand_sort
    IS '商品类别排序';

-- Table: public.t_product_category

-- DROP TABLE public.t_product_category;

CREATE TABLE public.t_product_category
(
    category_id bigint NOT NULL,
    category_name character varying(10) COLLATE pg_catalog."default",
    category_sort integer,
    is_deleted smallint,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    remarks character varying(255) COLLATE pg_catalog."default",
    versions bigint DEFAULT 0,
    CONSTRAINT "`t_product_category_pkey" PRIMARY KEY (category_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_product_category
    OWNER to postgres;
COMMENT ON TABLE public.t_product_category
    IS '商品类别表';

COMMENT ON COLUMN public.t_product_category.category_name
    IS '商品类别名称';

COMMENT ON COLUMN public.t_product_category.category_sort
    IS '商品类别排序';

-- Table: public.t_commodity

-- DROP TABLE public.t_commodity;

CREATE TABLE public.t_commodity
(
    commodity_id bigint,
    commodity_name character varying(8) COLLATE pg_catalog."default",
    brand_id bigint,
    brand_name character varying(10) COLLATE pg_catalog."default",
    category_id bigint,
    category_name character varying(10) COLLATE pg_catalog."default",
    price money,
    num bigint,
    sales_volume bigint,
    evaluation_num bigint,
    production_site character varying(50) COLLATE pg_catalog."default",
    license_number character varying(20) COLLATE pg_catalog."default",
    shelf_life bigint,
    status smallint,
    is_deleted smallint,
    remarks character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    versions bigint
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_commodity
    OWNER to postgres;
COMMENT ON TABLE public.t_commodity
    IS '商品表';

COMMENT ON COLUMN public.t_commodity.commodity_name
    IS '商品名称';

COMMENT ON COLUMN public.t_commodity.brand_id
    IS '品牌id';

COMMENT ON COLUMN public.t_commodity.brand_name
    IS '品牌名称';

COMMENT ON COLUMN public.t_commodity.category_id
    IS '类别id';

COMMENT ON COLUMN public.t_commodity.category_name
    IS '类别名称';

COMMENT ON COLUMN public.t_commodity.price
    IS '价格';

COMMENT ON COLUMN public.t_commodity.sales_volume
    IS '销量';

COMMENT ON COLUMN public.t_commodity.evaluation_num
    IS '评价数量';

COMMENT ON COLUMN public.t_commodity.production_site
    IS '生产地';

COMMENT ON COLUMN public.t_commodity.license_number
    IS '产品生产编号';

COMMENT ON COLUMN public.t_commodity.shelf_life
    IS '保质期';

COMMENT ON COLUMN public.t_commodity.description
    IS '描述';

COMMENT ON COLUMN public.t_commodity.num
    IS '商品数量';

COMMENT ON COLUMN public.t_commodity.status
    IS '商品状态；0-未上架；1-已上架；2-已下架；';