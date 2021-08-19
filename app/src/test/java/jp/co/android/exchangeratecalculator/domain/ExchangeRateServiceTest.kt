package jp.co.android.exchangeratecalculator.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ExchangeRateServiceTest {

    private val remoteRepositoryImpl: ExchangeRateRemoteRepository = mockk(relaxed = true)
    private val localRepository: ExchangeRateLocalRepository = mockk(relaxed = true)
    private val time: TimeService = mockk(relaxed = true)
    private lateinit var service: ExchangeRateService

    @Before
    fun setUp() {
        service = ExchangeRateService(
            remoteRepositoryImpl,
            localRepository,
            time
        )
    }

    @Test
    fun load_remote() {
        // リモートレポジトリでロードするように設定
        every {
            time.shouldLoadFromRemote()
        } returns true
        // 実行
        service.load()
        verify {
            remoteRepositoryImpl.load()
        }
    }

    @Test
    fun load_local() {
        // ローカルレポジトリでロードするように設定
        every {
            time.shouldLoadFromRemote()
        } returns false
        // 実行
        service.load()
        verify {
            localRepository.load()
        }
    }
}