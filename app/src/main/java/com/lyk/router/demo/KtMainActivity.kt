package com.lyk.router.demo

import android.app.Activity
import com.lyk.router.annotations.Destination

@Destination(
        url = "router://page-kotlin",
        description = "登录页面"
)
class KtMainActivity : Activity() {
}