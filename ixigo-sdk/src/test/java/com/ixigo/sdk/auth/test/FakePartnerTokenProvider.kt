package com.ixigo.sdk.auth.test

import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenProvider

class FakePartnerTokenProvider(override val partnerToken: PartnerToken?) : PartnerTokenProvider
