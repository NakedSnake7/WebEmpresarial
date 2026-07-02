package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.FeatureAccessPolicy;  
import com.webempresarial.store.feature.FeatureCategory;
import com.webempresarial.store.feature.FeatureDefinition;
import com.webempresarial.store.feature.FeaturePresentation;
import com.webempresarial.store.feature.ModuleDefinition;
import com.webempresarial.store.feature.PlatformModule;
import com.webempresarial.store.feature.automation.AutomationDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.StorePlan;
import com.webempresarial.store.feature.ModuleLifecycleContext;
import com.webempresarial.store.feature.permission.PermissionDefinition;

import org.springframework.stereotype.Component;

@Component
public class CrmFeatures implements PlatformModule {

    @Override
    public ModuleDefinition definition() {
        return ModuleDefinition.builder("CRM")
                .description("Módulo comercial para leads, pipeline, tareas y propuestas.")

                .feature(crm())
                .feature(leads())
                .feature(pipeline())
                .feature(tasks())
                .feature(proposals())

                .sidebarSection(
                        SidebarSectionDefinition.builder("CRM Comercial", "📊")
                                .item("CRM Dashboard", "📊", "/crm/dashboard", Feature.CRM)
                                .item("Pipeline", "🎯", "/crm/pipeline", Feature.PIPELINE)
                                .item("Leads", "🗂️", "/admin/leads", Feature.LEADS)
                                .item("Tareas", "✅", "/crm/tasks", Feature.TASKS)
                                .item("Propuestas", "📄", "/crm/proposals", Feature.PROPOSALS)
                                .build()
                )

                .build();
    }

    private FeatureDefinition crm() {
        return FeatureDefinition.builder(Feature.CRM)
                .displayName("CRM")
                .shortName("CRM")
                .slug("crm")
                .description("Gestiona leads y oportunidades comerciales.")
                .category(FeatureCategory.CRM)
                .icon("📊")
                .color("primary")
                .url("/crm/dashboard")
                .healthWeight(8)
                .order(100)
                .accessPolicy(pro())
                .presentation(standard(true))
                .build();
    }

    private FeatureDefinition leads() {
        return FeatureDefinition.builder(Feature.LEADS)
                .displayName("Leads")
                .shortName("Leads")
                .slug("leads")
                .description("Captura y administra prospectos.")
                .category(FeatureCategory.CRM)
                .icon("🗂️")
                .color("primary")
                .url("/admin/leads")
                .healthWeight(6)
                .order(110)
                .accessPolicy(pro())
                .presentation(standard(true))
                .build();
    }

    private FeatureDefinition pipeline() {
        return FeatureDefinition.builder(Feature.PIPELINE)
                .displayName("Pipeline")
                .shortName("Pipeline")
                .slug("pipeline")
                .description("Visualiza oportunidades comerciales por etapa.")
                .category(FeatureCategory.CRM)
                .icon("🎯")
                .color("primary")
                .url("/crm/pipeline")
                .healthWeight(6)
                .order(120)
                .accessPolicy(pro())
                .presentation(standard(true))
                .build();
    }

    private FeatureDefinition tasks() {
        return FeatureDefinition.builder(Feature.TASKS)
                .displayName("Tareas")
                .shortName("Tareas")
                .slug("tasks")
                .description("Gestiona seguimientos comerciales y pendientes.")
                .category(FeatureCategory.CRM)
                .icon("✅")
                .color("primary")
                .url("/crm/tasks")
                .healthWeight(4)
                .order(130)
                .accessPolicy(pro())
                .presentation(standard(false))
                .build();
    }

    private FeatureDefinition proposals() {
        return FeatureDefinition.builder(Feature.PROPOSALS)
                .displayName("Propuestas")
                .shortName("Propuestas")
                .slug("proposals")
                .description("Genera propuestas comerciales para tus leads.")
                .category(FeatureCategory.CRM)
                .icon("📄")
                .color("primary")
                .url("/crm/proposals")
                .healthWeight(6)
                .order(140)
                .accessPolicy(pro())
                .presentation(standard(true))
                .build();
    }

    private FeatureAccessPolicy pro() {
        return FeatureAccessPolicy.builder()
                .minimumPlan(StorePlan.PRO)
                .premium(true)
                .enabled(true)
                .build();
    }

    private FeaturePresentation standard(boolean showInDashboard) {
        return FeaturePresentation.builder()
                .showInSidebar(true)
                .showInDashboard(showInDashboard)
                .showInBilling(true)
                .showUpgradeCard(true)
                .trackUsage(true)
                .build();
    }
    @Override
    public void boot(ModuleLifecycleContext context) {
        context.permissionRegistry().register(
                new PermissionDefinition(
                        "crm.read",
                        "Leer CRM",
                        "Permite consultar dashboard, leads y pipeline.",
                        Feature.CRM
                )
        );

        context.permissionRegistry().register(
                new PermissionDefinition(
                        "crm.write",
                        "Gestionar CRM",
                        "Permite crear, editar y mover leads dentro del CRM.",
                        Feature.CRM
                )
        );

        context.permissionRegistry().register(
                new PermissionDefinition(
                        "proposals.manage",
                        "Gestionar propuestas",
                        "Permite crear, enviar, aceptar y rechazar propuestas.",
                        Feature.PROPOSALS
                )
        );
        context.automationRegistry().register(
                new AutomationDefinition(
                        "crm.lead.created.task",
                        "Crear tarea al recibir lead",
                        "Crea una tarea automática cuando entra un nuevo lead.",
                        Feature.LEADS,
                        "LEAD_CREATED"
                )
        );

        context.automationRegistry().register(
                new AutomationDefinition(
                        "crm.proposal.sent.followup",
                        "Seguimiento de propuesta enviada",
                        "Crea seguimiento automático 24 horas después de enviar una propuesta.",
                        Feature.PROPOSALS,
                        "PROPOSAL_SENT"
                )
        );

        context.automationRegistry().register(
                new AutomationDefinition(
                        "crm.lead.created.notification",
                        "Notificación de nuevo lead",
                        "Notifica al administrador cuando entra un nuevo lead.",
                        Feature.LEADS,
                        "LEAD_CREATED"
                )
        );
    }
}