package com.apptreo.export.sms

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Message(var id: Int, var address: String, var type: String, var body: String, date: String?) {
    var date: String
    override fun toString(): String {
        return "id= $id, address= $address, type= $type, body= $body, date= $date"
    }

    init {
        val obj: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z")
        val res = Date(date)
        this.date = obj.format(res)
    }
}