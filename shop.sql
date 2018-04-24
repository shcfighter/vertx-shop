--
set lc_monetary='zh_CN.UTF-8';

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
    is_deleted smallint DEFAULT 0,
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
    is_deleted smallint DEFAULT 0,
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
    is_deleted smallint DEFAULT 0,
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
    commodity_id bigint NOT NULL,
    commodity_name character varying(100) COLLATE pg_catalog."default",
    brand_id bigint,
    brand_name character varying(50) COLLATE pg_catalog."default",
    category_id bigint,
    category_name character varying(50) COLLATE pg_catalog."default",
    price money,
    original_price money,
    num bigint,
    freeze_num bigint DEFAULT 0,
    -- license_number character varying(20) COLLATE pg_catalog."default",
    status smallint,
    image_url text COLLATE pg_catalog."default",
    freight money,
    is_deleted smallint DEFAULT 0,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    description character varying(500) COLLATE pg_catalog."default",
    remarks character varying(255) COLLATE pg_catalog."default",
    versions bigint DEFAULT 0,
    CONSTRAINT t_commodity_pkey PRIMARY KEY (commodity_id)
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

-- COMMENT ON COLUMN public.t_commodity.license_number
   -- IS '产品生产编号';

COMMENT ON COLUMN public.t_commodity.description
    IS '描述';

COMMENT ON COLUMN public.t_commodity.num
    IS '商品数量';

COMMENT ON COLUMN public.t_commodity.freeze_num
    IS '冻结数量';

COMMENT ON COLUMN public.t_commodity.original_price
    IS '原价';

COMMENT ON COLUMN public.t_commodity.image_url
    IS '商品图';

COMMENT ON COLUMN public.t_commodity.freight
    IS '快递费';


-- Table: public.t_order

-- DROP TABLE public.t_order;

CREATE TABLE public.t_order
(
    order_id bigint NOT NULL,
    user_id bigint,
    shipping_information_id bigint,
    create_time timestamp without time zone,
    order_details json,
    total_price money NOT NULL DEFAULT 0,
    order_status integer,
    cancel_time timestamp without time zone,
    send_time timestamp with time zone,
    is_deleted smallint DEFAULT 0,
    remarks character varying(255) COLLATE pg_catalog."default",
    versions bigint DEFAULT 0,
    leave_message character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT t_order_pkey PRIMARY KEY (order_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_order
    OWNER to postgres;
COMMENT ON TABLE public.t_order
    IS '订单';

COMMENT ON COLUMN public.t_order.user_id
    IS '用户编号';

COMMENT ON COLUMN public.t_order.shipping_information_id
    IS '收货地址编号';

COMMENT ON COLUMN public.t_order.create_time
    IS '下单时间';

COMMENT ON COLUMN public.t_order.order_details
    IS '订单详情；如：[{"commodity_id": 123456, "order_num": 5}, {}]';

COMMENT ON COLUMN public.t_order.total_price
    IS '总价格';

COMMENT ON COLUMN public.t_order.order_status
    IS '订单状态	1=有效	2=已取消订单	3=已退款	4=已发货	0=订单失效';

COMMENT ON COLUMN public.t_order.cancel_time
    IS '订单取消时间';

COMMENT ON COLUMN public.t_order.send_time
    IS '发货时间';

COMMENT ON COLUMN public.t_user_info.is_deleted
    IS '0-未删除；1-删除';

COMMENT ON COLUMN public.t_order.remarks
    IS '备注';

COMMENT ON COLUMN public.t_order.leave_message
    IS '买家留言';


-- Table: public.t_order_commodity_log

-- DROP TABLE public.t_order_commodity_log;

CREATE TABLE public.t_order_commodity_log
(
    log_id bigint NOT NULL,
    order_id bigint NOT NULL,
    commodity_id bigint NOT NULL,
    ip inet,
    num bigint DEFAULT 0,
    is_deleted smallint DEFAULT 0,
    create_time timestamp without time zone,
    remarks text COLLATE pg_catalog."default",
    CONSTRAINT t_order_commodity_log_pkey PRIMARY KEY (log_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_order_commodity_log
    OWNER to postgres;

COMMENT ON COLUMN public.t_order_commodity_log.order_id
    IS '订单id';

COMMENT ON COLUMN public.t_order_commodity_log.commodity_id
    IS '商品id';

COMMENT ON COLUMN public.t_order_commodity_log.ip
    IS 'ip地址';

COMMENT ON COLUMN public.t_order_commodity_log.num
    IS '订单购买数量';

COMMENT ON COLUMN public.t_order_commodity_log.create_time
    IS '下单时间';


-- Table: public.t_user_info

-- DROP TABLE public.t_user_info;

CREATE TABLE public.t_user_info
(
    user_info_id bigint NOT NULL,
    user_id bigint,
    real_name character varying(20) COLLATE pg_catalog."default",
    sex integer,
    birthday date,
    photo_url character varying(512) COLLATE pg_catalog."default",
    address character varying(512) COLLATE pg_catalog."default",
    qq character varying(50) COLLATE pg_catalog."default",
    weibo character varying(50) COLLATE pg_catalog."default",
    invite_code character varying(50) COLLATE pg_catalog."default",
    is_deleted smallint,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    remarks character varying(256) COLLATE pg_catalog."default",
    versions bigint NOT NULL DEFAULT 0
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.t_user_info
    OWNER to postgres;
COMMENT ON TABLE public.t_user_info
    IS '用户详情表';

COMMENT ON COLUMN public.t_user_info.user_id
    IS '用户id';

COMMENT ON COLUMN public.t_user_info.real_name
    IS '真实姓名';

COMMENT ON COLUMN public.t_user_info.sex
    IS '性别   0-男；1-女；2-保密';

COMMENT ON COLUMN public.t_user_info.birthday
    IS '生日';

COMMENT ON COLUMN public.t_user_info.photo_url
    IS '头像url';

COMMENT ON COLUMN public.t_user_info.address
    IS '家庭地址';

COMMENT ON COLUMN public.t_user_info.invite_code
    IS '邀请码';

COMMENT ON COLUMN public.t_user_info.is_deleted
    IS '0-未删除；1-删除';

COMMENT ON COLUMN public.t_user_info.versions
    IS '版本号';