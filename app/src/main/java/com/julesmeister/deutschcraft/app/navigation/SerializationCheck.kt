package com.julesmeister.deutschcraft.navigation

import com.julesmeister.deutschcraft.app.navigation.ChatDestination
import com.julesmeister.deutschcraft.app.navigation.ProgressDestination
import com.julesmeister.deutschcraft.app.navigation.ThreadListDestination

internal val threadListSerializerCheck =
    ThreadListDestination.serializer()

internal val chatSerializerCheck =
    ChatDestination.serializer()

internal val progressSerializerCheck =
    ProgressDestination.serializer()