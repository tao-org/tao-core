-- Table: agent.conversation
DROP TABLE IF EXISTS agent.messages;
DROP TABLE IF EXISTS agent.conversation;
CREATE TABLE IF NOT EXISTS agent.conversation
(
    id character varying (255) NOT NULL,
    user_id character varying (50) NOT NULL,
    provider character varying (255) NOT NULL,
    name character varying(255),
    summary text,
    created timestamp without time zone DEFAULT now(),
    last_updated timestamp without time zone DEFAULT now()
);
ALTER TABLE agent.conversation ADD CONSTRAINT PK_conversation PRIMARY KEY (id);
ALTER TABLE agent.conversation ADD CONSTRAINT fk_conversation_user FOREIGN KEY (user_id)
    REFERENCES usr."user" (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

-- Table: agent.messages
CREATE TABLE IF NOT EXISTS agent.messages
(
    id character varying (255) NOT NULL,
    conversation_id character varying (255) NOT NULL,
    role character varying (20) NOT NULL,
    content text NOT NULL,
    archived boolean default false,
    created timestamp without time zone DEFAULT now(),
    last_updated timestamp without time zone DEFAULT now()
);
ALTER TABLE agent.messages ADD CONSTRAINT PK_messages PRIMARY KEY (id);
ALTER TABLE agent.messages ADD CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
    REFERENCES agent.conversation (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
CREATE INDEX idx_agent_conversation_user ON agent.conversation (user_id, last_updated DESC);
CREATE INDEX idx_agent_messages_conversation ON agent.messages (conversation_id, archived, created);
