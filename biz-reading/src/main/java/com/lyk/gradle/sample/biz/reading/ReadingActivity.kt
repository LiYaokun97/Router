package com.lyk.gradle.sample.biz.reading

import android.app.Activity
import com.lyk.router.annotations.Destination

@Destination(
        url = "router://reading",
        description = "阅读页"
)
class ReadingActivity : Activity() {

}