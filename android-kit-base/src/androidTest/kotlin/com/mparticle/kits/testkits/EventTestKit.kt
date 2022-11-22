package com.mparticle.kits.testkits

import com.mparticle.MPEvent
import com.mparticle.kits.KitIntegration
import com.mparticle.kits.ReportingMessage

class EventTestKit : ListenerTestKit(), KitIntegration.EventListener {
    var onLogEvent: (MPEvent) -> MutableList<ReportingMessage>? = { null }


    override fun logEvent(event: MPEvent?): List<ReportingMessage?>? {
        return event?.let { onLogEvent(it) }
    }

    override fun leaveBreadcrumb(breadcrumb: String?): MutableList<ReportingMessage> {
        TODO("Not yet implemented")
    }

    override fun logError(
        message: String?,
        errorAttributes: Map<String, String>?
    ): List<ReportingMessage?>? {
        TODO("Not yet implemented")
    }

    override fun logException(
        exception: Exception?,
        exceptionAttributes: Map<String, String>?,
        message: String?
    ): List<ReportingMessage?>? {
        TODO("Not yet implemented")
    }

    override fun logScreen(
        screenName: String?,
        screenAttributes: Map<String?, String?>?
    ): List<ReportingMessage?>? {
        TODO("Not yet implemented")
    }
}
