package com.ixigo.sdk.test.logging

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import timber.log.Timber

class FakeTree: Timber.Tree() {
    val logs: MutableList<FakeLog> = mutableListOf()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logs.add(FakeLog(priority, message, t?.javaClass, tag))
    }

    fun assertLastLog(log: FakeLog) {
        assertThat(logs.last(), FakeLogMatcher(log))
    }
}

fun withFakeTree(body: FakeTree.() -> Unit) {
    val testTree = FakeTree()
    Timber.plant(testTree)
    body(testTree)
    Timber.uproot(testTree)
}

data class FakeLog(val priority: Int, val message: String, val t: Class<out Throwable>? = null, val tag: String? = null)

private class FakeLogMatcher(val expectedLog: FakeLog): BaseMatcher<FakeLog>() {
    override fun describeTo(description: Description?) {
        description?.appendText(expectedLog.toString())
    }

    override fun matches(item: Any?): Boolean {
        val log = item as FakeLog? ?: return false
        return log.message.startsWith(expectedLog.message) &&
                expectedLog.priority == log.priority &&
                expectedLog.tag ==  log.tag &&
                expectedLog.t == log.t

    }
}