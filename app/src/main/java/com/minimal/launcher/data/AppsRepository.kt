package com.minimal.launcher.data

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.minimal.launcher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsRepository(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    suspend fun getInstalledApps(launcherPackageName: String): List<AppItem> = withContext(Dispatchers.IO) {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfoList: List<ResolveInfo> = try {
            packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            emptyList()
        }
        
        val appItems = mutableListOf<AppItem>()
        
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo ?: continue
            val packageName = activityInfo.packageName
            
            // Skip the launcher itself
            if (packageName == launcherPackageName) {
                continue
            }
            
            val label = resolveInfo.loadLabel(packageManager).toString()
            val componentName = activityInfo.name
            
            appItems.add(
                AppItem(
                    label = label,
                    packageName = packageName,
                    componentName = componentName
                )
            )
        }
        
        // Sort alphabetically by label
        appItems.sortedBy { it.label.lowercase() }
    }
    
    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun openAppInfo(packageName: String) = withContext(Dispatchers.IO) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
}
