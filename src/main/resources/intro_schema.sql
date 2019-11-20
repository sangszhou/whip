DROP TABLE IF EXISTS pipeline_template, pipeline, stage;
 
CREATE TABLE `pipeline_template` (
  `id` varchar(256) NOT NULL,
  `gmt_create` datetime NOT NULL,
  `gmt_modified` datetime NOT NULL,
  `trigger_interval` int(11) NOT NULL DEFAULT '600',
  `content` varchar(4096) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
 
-- 需要添加 index
-- CREATE TABLE pipeline (
--   id             INT          NOT NULL PRIMARY KEY,
--   template_id    INT          NOT NULL,
--   start_time     TIME         NOT NULL,
--   end_time       TIME,
--   status         VARCHAR(100) NOT NULL
-- );
--  
--
-- CREATE TABLE stage (
--   id             INT           NOT NULL PRIMARY KEY,
--   ref_id         INT           NOT NULL,
--   name           VARCHAR(1024) NOT NULL,
--   type           VARCHAR(1024) NOT NULL,
--   context        VARCHAR(4096) NOT NULL,
--   output         VARCHAR(4096) NOT NULL,
--   required       VARCHAR(4096) NOT NULL,
--   start_time     TIME          NOT NULL,
--   end_time       TIME,
--   status         VARCHAR(100)  NOT NULL
-- );


-- CREATE TABLE author_book (
--   author_id      INT          NOT NULL,
--   book_id        INT          NOT NULL,
--    
--   PRIMARY KEY (author_id, book_id),
--   CONSTRAINT fk_ab_author     FOREIGN KEY (author_id)  REFERENCES author (id) 
--     ON UPDATE CASCADE ON DELETE CASCADE,
--   CONSTRAINT fk_ab_book       FOREIGN KEY (book_id)    REFERENCES book   (id)
-- );
--  
-- INSERT INTO author VALUES
--   (1, 'Kathy', 'Sierra'),
--   (2, 'Bert', 'Bates'),
--   (3, 'Bryan', 'Basham');
--  
-- INSERT INTO book VALUES
--   (1, 'Head First Java'),
--   (2, 'Head First Servlets and JSP'),
--   (3, 'OCA/OCP Java SE 7 Programmer');
--  
-- INSERT INTO author_book VALUES (1, 1), (1, 3), (2, 1);