CREATE TABLE transformation_strategic_synthesis_reviews (

    id BIGINT NOT NULL AUTO_INCREMENT,

    reviewed_synthesis_id BIGINT NOT NULL,

    resulting_synthesis_id BIGINT NOT NULL,

    reviewer VARCHAR(180) NOT NULL,

    reviewer_type VARCHAR(40) NOT NULL,

    decision VARCHAR(30) NOT NULL,

    reason TEXT NOT NULL,

    reviewed_at TIMESTAMP(6) NOT NULL,

    previous_status VARCHAR(30) NOT NULL,

    resulting_status VARCHAR(30) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_strategic_review_reviewed_synthesis
        FOREIGN KEY (reviewed_synthesis_id)
        REFERENCES transformation_strategic_syntheses (id),

    CONSTRAINT fk_strategic_review_resulting_synthesis
        FOREIGN KEY (resulting_synthesis_id)
        REFERENCES transformation_strategic_syntheses (id)
);

CREATE INDEX idx_strategic_synthesis_review_reviewed
    ON transformation_strategic_synthesis_reviews (
        reviewed_synthesis_id,
        reviewed_at
    );

CREATE INDEX idx_strategic_synthesis_review_resulting
    ON transformation_strategic_synthesis_reviews (
        resulting_synthesis_id
    );

CREATE INDEX idx_strategic_synthesis_review_reviewer
    ON transformation_strategic_synthesis_reviews (
        reviewer
    );