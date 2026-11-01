package com.wangzan101.mika.cap.phone.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Agent 虚拟屏的用户活动保活:模型的所有操作都是注入事件,系统不计用户活动,
 * 而 Agent 屏是独立 display group,不跟着主屏的亮灭走——从建好起没有任何真实用户活动,
 * 系统(Android 15 起,有的厂商更激进)会按闲置把它熄掉:停止合成、不再出帧,
 * 画面和截图一起卡死,继续注入和重启 App 都救不回来(见 issue #15)。
 * 屏存在期间由 helper(shell 身份)每隔几秒替它"活动"一次,点亮它自己的屏、不碰用户主屏。
 */
class DisplayKeepAliveTest {
    @Test fun pingsEveryOwnDisplayOnApi31AndLater() {
        assertEquals(listOf(3, 7), ShellService.keepAliveDisplayIds(sdk = 31, displays = listOf(7, 3)))
        assertEquals(listOf(5), ShellService.keepAliveDisplayIds(sdk = 36, displays = listOf(5)))
    }

    @Test fun noPingsBeforeApi31() {
        // 31 之前隐藏接口 userActivity 没有 displayId 参数(调了会点到用户主屏);那时也没有虚拟屏闲置回收
        assertTrue(ShellService.keepAliveDisplayIds(sdk = 30, displays = listOf(3, 7)).isEmpty())
    }

    @Test fun intervalStaysWellUnderAnyIdleTimeout() {
        // scrcpy --keep-active 同款 4 秒:再短浪费,再长可能落在厂商更激进的闲置判定之后
        assertEquals(4_000L, ShellService.KEEP_ALIVE_INTERVAL_MS)
    }
}
