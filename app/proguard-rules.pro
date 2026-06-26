# 纪念日 ProGuard 规则
# 保留 Gson 序列化的数据模型
-keep class com.memorialday.app.models.** { *; }
-keepclassmembers class com.memorialday.app.models.** { *; }
