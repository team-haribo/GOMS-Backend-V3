package com.teamharibo.goms.domain.outing.service.impl

import com.teamharibo.goms.domain.outing.dto.response.OutingCountResponse
import com.teamharibo.goms.domain.outing.repository.OutingRepository
import com.teamharibo.goms.domain.outing.service.OutingCountService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutingCountServiceImpl(
    private val outingRepository: OutingRepository
) : OutingCountService {

    @Transactional(readOnly = true)
    override fun getCount(): OutingCountResponse {
        return OutingCountResponse(outingRepository.countActive())
    }
}
