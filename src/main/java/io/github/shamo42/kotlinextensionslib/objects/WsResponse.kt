/*
 * Copyright (c) 2018-present, Wiltgen Philippe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.shamo42.kotlinextensionslib.objects

import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString


open class WsResponse(val webSocket: WebSocket,
                      val type: WsResponseType,
                      val response: Response? = null,
                      val code: Int = -1,
                      val text: String? = null,
                      val byteArray: ByteString? = null) {

    companion object {
        fun open(webSocket: WebSocket, response: Response): WsOpen {
            return WsOpen(webSocket, response)
        }
        fun closing(webSocket: WebSocket, code: Int, reason: String): WsClosing {
            return WsClosing(webSocket, code, reason)
        }
        fun textMessage(webSocket: WebSocket, message: String): WsTextMessage {
            return WsTextMessage(webSocket, message)
        }
        fun byteMessage(webSocket: WebSocket, message: ByteString): WsByteMessage {
            return WsByteMessage(webSocket, message)
        }
        fun closed(webSocket: WebSocket, code: Int, reason: String): WsClosed {
            return WsClosed(webSocket, code, reason)
        }
        fun failure(webSocket: WebSocket, response: Response): WsFailure {
            return WsFailure(webSocket, response)
        }
    }
}

class WsOpen(webSocket: WebSocket, response: Response): WsResponse(webSocket, WsResponseType.TYPE_OPEN, response)
class WsClosing(webSocket: WebSocket, code: Int, reason: String): WsResponse(webSocket, WsResponseType.TYPE_CLOSING, code = code, text = reason)
class WsTextMessage(webSocket: WebSocket, data: String): WsResponse(webSocket, WsResponseType.TYPE_MESSAGE_STRING, text = data)
class WsByteMessage(webSocket: WebSocket, byteArray: ByteString): WsResponse(webSocket, WsResponseType.TYPE_MESSAGE_BYTE, byteArray = byteArray)
class WsClosed(webSocket: WebSocket, code: Int, reason: String): WsResponse(webSocket, WsResponseType.TYPE_CLOSED, code = code, text = reason)
class WsFailure(webSocket: WebSocket, response: Response): WsResponse(webSocket, WsResponseType.TYPE_FAILURE, response)

enum class WsResponseType(val type: Int) {
    TYPE_OPEN(1),
    TYPE_CLOSING(2),
    TYPE_CLOSED(3),
    TYPE_MESSAGE_BYTE(4),
    TYPE_MESSAGE_STRING(5),
    TYPE_FAILURE(6)
}

