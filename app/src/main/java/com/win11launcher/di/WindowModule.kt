package com.win11launcher.di

import com.win11launcher.navigation.WindowRouter
import com.win11launcher.services.WindowManager
import com.win11launcher.ui.chat.ChatWindowContent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WindowModule {
    
    @Provides
    @Singleton
    fun provideWindowManager(): WindowManager {
        return WindowManager()
    }
    
    @Provides
    @Singleton
    fun provideWindowRouter(windowManager: WindowManager): WindowRouter {
        val router = WindowRouter(windowManager)
        
        // Register window content for all destinations
        registerWindowContent(router)
        
        return router
    }
    
    private fun registerWindowContent(router: WindowRouter) {
        // Register AI Chat content
        router.registerContent("ai_chat") {
            ChatWindowContent()
        }
        
        // Register other window contents here as they're implemented
        // router.registerContent("file_manager") { FileManagerContent() }
        // router.registerContent("settings") { SettingsContent() }
        // router.registerContent("all_apps") { AllAppsContent() }
    }
}