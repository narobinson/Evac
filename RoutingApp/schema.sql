--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

-- Started on 2017-04-23 18:15:38

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12387)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 3676 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 3 (class 3079 OID 26433)
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- TOC entry 3677 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- TOC entry 2 (class 3079 OID 26556)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 3678 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

--
-- TOC entry 1437 (class 1255 OID 28197)
-- Name: osmosisupdate(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION osmosisupdate() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
END;
$$;


ALTER FUNCTION public.osmosisupdate() OWNER TO postgres;

--
-- TOC entry 1438 (class 1255 OID 28030)
-- Name: unnest_bbox_way_nodes(); Type: FUNCTION; Schema: public; Owner: OSM
--

CREATE FUNCTION unnest_bbox_way_nodes() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	previousId ways.id%TYPE;
	currentId ways.id%TYPE;
	result bigint[];
	wayNodeRow way_nodes%ROWTYPE;
	wayNodes ways.nodes%TYPE;
BEGIN
	FOR wayNodes IN SELECT bw.nodes FROM bbox_ways bw LOOP
		FOR i IN 1 .. array_upper(wayNodes, 1) LOOP
			INSERT INTO bbox_way_nodes (id) VALUES (wayNodes[i]);
		END LOOP;
	END LOOP;
END;
$$;


ALTER FUNCTION public.unnest_bbox_way_nodes() OWNER TO "OSM";

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 209 (class 1259 OID 28232)
-- Name: app_user_location_groups; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE app_user_location_groups (
    id bigint NOT NULL,
    lat numeric(6,4) NOT NULL,
    lon numeric(7,4) NOT NULL,
    count integer NOT NULL
);


ALTER TABLE app_user_location_groups OWNER TO "OSM";

--
-- TOC entry 210 (class 1259 OID 28237)
-- Name: app_user_routes; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE app_user_routes (
    id bigint NOT NULL,
    route text NOT NULL,
    last_visited_node bigint
);


ALTER TABLE app_user_routes OWNER TO "OSM";

--
-- TOC entry 211 (class 1259 OID 28250)
-- Name: app_users; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE app_users (
    id bigint NOT NULL,
    lat numeric(10,8) NOT NULL,
    lon numeric(11,8) NOT NULL,
    uid uuid NOT NULL,
    user_group bigint NOT NULL,
    route bigint
);


ALTER TABLE app_users OWNER TO "OSM";

--
-- TOC entry 204 (class 1259 OID 28153)
-- Name: nodes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE nodes (
    id bigint NOT NULL,
    version integer NOT NULL,
    user_id integer NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore,
    geom geometry(Point,4326)
);


ALTER TABLE nodes OWNER TO postgres;

--
-- TOC entry 208 (class 1259 OID 28174)
-- Name: relation_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE relation_members (
    relation_id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_type character(1) NOT NULL,
    member_role text NOT NULL,
    sequence_id integer NOT NULL
);
ALTER TABLE ONLY relation_members ALTER COLUMN relation_id SET (n_distinct=-0.09);
ALTER TABLE ONLY relation_members ALTER COLUMN member_id SET (n_distinct=-0.62);
ALTER TABLE ONLY relation_members ALTER COLUMN member_role SET (n_distinct=6500);
ALTER TABLE ONLY relation_members ALTER COLUMN sequence_id SET (n_distinct=10000);


ALTER TABLE relation_members OWNER TO postgres;

--
-- TOC entry 207 (class 1259 OID 28168)
-- Name: relations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE relations (
    id bigint NOT NULL,
    version integer NOT NULL,
    user_id integer NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore
);


ALTER TABLE relations OWNER TO postgres;

--
-- TOC entry 202 (class 1259 OID 28144)
-- Name: schema_info; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE schema_info (
    version integer NOT NULL
);


ALTER TABLE schema_info OWNER TO postgres;

--
-- TOC entry 203 (class 1259 OID 28147)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE users (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE users OWNER TO postgres;

--
-- TOC entry 206 (class 1259 OID 28165)
-- Name: way_nodes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE way_nodes (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id integer NOT NULL
);
ALTER TABLE ONLY way_nodes ALTER COLUMN way_id SET (n_distinct=-0.08);
ALTER TABLE ONLY way_nodes ALTER COLUMN node_id SET (n_distinct=-0.83);
ALTER TABLE ONLY way_nodes ALTER COLUMN sequence_id SET (n_distinct=2000);


ALTER TABLE way_nodes OWNER TO postgres;

--
-- TOC entry 205 (class 1259 OID 28159)
-- Name: ways; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE ways (
    id bigint NOT NULL,
    version integer NOT NULL,
    user_id integer NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore,
    nodes bigint[]
);


ALTER TABLE ways OWNER TO postgres;

--
-- TOC entry 3538 (class 2606 OID 28236)
-- Name: app_user_location_groups UserLocationGroup_pkey; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_user_location_groups
    ADD CONSTRAINT "UserLocationGroup_pkey" PRIMARY KEY (id);


--
-- TOC entry 3540 (class 2606 OID 28244)
-- Name: app_user_routes UserRoutes_pkey; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_user_routes
    ADD CONSTRAINT "UserRoutes_pkey" PRIMARY KEY (id);


--
-- TOC entry 3542 (class 2606 OID 28254)
-- Name: app_users Users_pkey; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_users
    ADD CONSTRAINT "Users_pkey" PRIMARY KEY (id);


--
-- TOC entry 3526 (class 2606 OID 28201)
-- Name: nodes pk_nodes; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT pk_nodes PRIMARY KEY (id);


--
-- TOC entry 3536 (class 2606 OID 28209)
-- Name: relation_members pk_relation_members; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);


--
-- TOC entry 3533 (class 2606 OID 28207)
-- Name: relations pk_relations; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT pk_relations PRIMARY KEY (id);


--
-- TOC entry 3521 (class 2606 OID 28181)
-- Name: schema_info pk_schema_info; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY schema_info
    ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);


--
-- TOC entry 3523 (class 2606 OID 28199)
-- Name: users pk_users; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);


--
-- TOC entry 3531 (class 2606 OID 28205)
-- Name: way_nodes pk_way_nodes; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);


--
-- TOC entry 3528 (class 2606 OID 28203)
-- Name: ways pk_ways; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT pk_ways PRIMARY KEY (id);


--
-- TOC entry 3524 (class 1259 OID 28210)
-- Name: idx_nodes_geom; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);


--
-- TOC entry 3534 (class 1259 OID 28212)
-- Name: idx_relation_members_member_id_and_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_relation_members_member_id_and_type ON relation_members USING btree (member_id, member_type);


--
-- TOC entry 3529 (class 1259 OID 28211)
-- Name: idx_way_nodes_node_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);


--
-- TOC entry 3543 (class 2606 OID 28245)
-- Name: app_user_routes FK_UserRoutes_Nodes; Type: FK CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_user_routes
    ADD CONSTRAINT "FK_UserRoutes_Nodes" FOREIGN KEY (last_visited_node) REFERENCES nodes(id);


--
-- TOC entry 3544 (class 2606 OID 28255)
-- Name: app_users FK_Users_UserLocationGroups; Type: FK CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_users
    ADD CONSTRAINT "FK_Users_UserLocationGroups" FOREIGN KEY (user_group) REFERENCES app_user_location_groups(id);


--
-- TOC entry 3545 (class 2606 OID 28260)
-- Name: app_users FK_Users_UserRoutes; Type: FK CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY app_users
    ADD CONSTRAINT "FK_Users_UserRoutes" FOREIGN KEY (route) REFERENCES app_user_routes(id);


-- Completed on 2017-04-23 18:15:39

--
-- PostgreSQL database dump complete
--

