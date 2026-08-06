-- ============================================================
-- V7__knowledge_dashboard_indexes.sql
-- Knowledge Engine™
-- Índices adicionales para Knowledge Dashboard API
-- ============================================================

-- ------------------------------------------------------------
-- 1. Actividad reciente por objeto de conocimiento
--
-- Optimiza consultas como:
--
-- WHERE knowledge_object_id = ?
-- ORDER BY created_at DESC
-- ------------------------------------------------------------

CREATE INDEX idx_knowledge_versions_object_created
    ON knowledge_object_versions (
        knowledge_object_id,
        created_at
    );


-- ------------------------------------------------------------
-- 2. Métricas y agrupaciones por contribuidor
--
-- Optimiza:
--   conteo de versiones por actor
--   top contributors
--   filtros por created_by
-- ------------------------------------------------------------

CREATE INDEX idx_knowledge_versions_created_by
    ON knowledge_object_versions (
        created_by
    );