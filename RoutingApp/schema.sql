--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.2
-- Dumped by pg_dump version 9.6.2

-- Started on 2017-04-17 15:59:06

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
-- TOC entry 3654 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 2 (class 3079 OID 19548)
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- TOC entry 3655 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- TOC entry 3 (class 3079 OID 18075)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 3656 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

--
-- TOC entry 1435 (class 1255 OID 19725)
-- Name: osmosisupdate(); Type: FUNCTION; Schema: public; Owner: OSM
--

CREATE FUNCTION osmosisupdate() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
END;
$$;


ALTER FUNCTION public.osmosisupdate() OWNER TO "OSM";

--
-- TOC entry 1434 (class 1255 OID 19724)
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
-- TOC entry 204 (class 1259 OID 19680)
-- Name: nodes; Type: TABLE; Schema: public; Owner: OSM
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


ALTER TABLE nodes OWNER TO "OSM";

--
-- TOC entry 208 (class 1259 OID 19701)
-- Name: relation_members; Type: TABLE; Schema: public; Owner: OSM
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


ALTER TABLE relation_members OWNER TO "OSM";

--
-- TOC entry 207 (class 1259 OID 19695)
-- Name: relations; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE relations (
    id bigint NOT NULL,
    version integer NOT NULL,
    user_id integer NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tags hstore
);


ALTER TABLE relations OWNER TO "OSM";

--
-- TOC entry 202 (class 1259 OID 19671)
-- Name: schema_info; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE schema_info (
    version integer NOT NULL
);


ALTER TABLE schema_info OWNER TO "OSM";

--
-- TOC entry 203 (class 1259 OID 19674)
-- Name: users; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE users (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE users OWNER TO "OSM";

--
-- TOC entry 206 (class 1259 OID 19692)
-- Name: way_nodes; Type: TABLE; Schema: public; Owner: OSM
--

CREATE TABLE way_nodes (
    way_id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id integer NOT NULL
);
ALTER TABLE ONLY way_nodes ALTER COLUMN way_id SET (n_distinct=-0.08);
ALTER TABLE ONLY way_nodes ALTER COLUMN node_id SET (n_distinct=-0.83);
ALTER TABLE ONLY way_nodes ALTER COLUMN sequence_id SET (n_distinct=2000);


ALTER TABLE way_nodes OWNER TO "OSM";

--
-- TOC entry 205 (class 1259 OID 19686)
-- Name: ways; Type: TABLE; Schema: public; Owner: OSM
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


ALTER TABLE ways OWNER TO "OSM";

--
-- TOC entry 3513 (class 2606 OID 19742)
-- Name: nodes pk_nodes; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT pk_nodes PRIMARY KEY (id);


--
-- TOC entry 3523 (class 2606 OID 19750)
-- Name: relation_members pk_relation_members; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id);


--
-- TOC entry 3520 (class 2606 OID 19748)
-- Name: relations pk_relations; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT pk_relations PRIMARY KEY (id);


--
-- TOC entry 3508 (class 2606 OID 19708)
-- Name: schema_info pk_schema_info; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY schema_info
    ADD CONSTRAINT pk_schema_info PRIMARY KEY (version);


--
-- TOC entry 3510 (class 2606 OID 19740)
-- Name: users pk_users; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);


--
-- TOC entry 3518 (class 2606 OID 19746)
-- Name: way_nodes pk_way_nodes; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id);


--
-- TOC entry 3515 (class 2606 OID 19744)
-- Name: ways pk_ways; Type: CONSTRAINT; Schema: public; Owner: OSM
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT pk_ways PRIMARY KEY (id);


--
-- TOC entry 3511 (class 1259 OID 19751)
-- Name: idx_nodes_geom; Type: INDEX; Schema: public; Owner: OSM
--

CREATE INDEX idx_nodes_geom ON nodes USING gist (geom);


--
-- TOC entry 3521 (class 1259 OID 19753)
-- Name: idx_relation_members_member_id_and_type; Type: INDEX; Schema: public; Owner: OSM
--

CREATE INDEX idx_relation_members_member_id_and_type ON relation_members USING btree (member_id, member_type);


--
-- TOC entry 3516 (class 1259 OID 19752)
-- Name: idx_way_nodes_node_id; Type: INDEX; Schema: public; Owner: OSM
--

CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id);


-- Completed on 2017-04-17 15:59:07

--
-- PostgreSQL database dump complete
--

