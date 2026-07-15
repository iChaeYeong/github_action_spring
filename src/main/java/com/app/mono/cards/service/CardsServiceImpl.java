package com.app.mono.cards.service;

import com.app.mono.cards.constants.CardsConstants;
import com.app.mono.cards.dto.CardsDto;
import com.app.mono.cards.entity.Cards;
import com.app.mono.cards.exception.CardAlreadyExistsException;
import com.app.mono.cards.mapper.CardsMapper;
import com.app.mono.cards.repository.CardsRepository;
import com.app.mono.common.exception.ResourceNotFoundException;
import com.app.mono.customers.entity.Customer;
import com.app.mono.customers.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardsServiceImpl implements CardsService {

    private final CardsRepository cardsRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void createCard(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );
        Optional<Cards> optionalCards = cardsRepository.findByCustomer(customer);

        // 이미 해당 휴대폰 번호로 카드가 등록되어 있으면 예외 발생
        if (optionalCards.isPresent()) {
            throw new CardAlreadyExistsException("해당 휴대폰 번호로 이미 카드가 등록되어 있습니다: " + mobileNumber);
        }

        // 새 카드 생성 후 저장
        cardsRepository.save(createNewCard(mobileNumber));
    }

    private Cards createNewCard(String mobileNumber) {
        Cards newCard = new Cards();

        // 임의 카드번호 생성
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));

        // 카드 기본 정보 세팅
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );
        newCard.setCustomer(customer);
        newCard.setCardType(CardsConstants.CREDIT_CARD);

        // 한도/사용금액/잔여금액 세팅
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);

        return newCard;
    }

    @Override
    public CardsDto fetchCard(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 휴대폰 번호로 카드 조회, 없으면 예외 발생
        Cards cards = cardsRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("카드", "휴대폰 번호", mobileNumber)
        );

        // Entity -> DTO 변환
//        System.out.println("cardsDto?????1>"+ cardsDto);
        System.out.println("cards####1>"+ cards);
        CardsDto cartsDto = new CardsDto();
        CardsMapper.mapToCardsDto(cards,cartsDto);
        System.out.println("cardsDto####2>"+ cartsDto);
        System.out.println("cards####13>"+ cards);
        return cartsDto;
    }

    @Override
    public boolean updateCard(CardsDto cardsDto) {
        // 카드번호로 카드 조회, 없으면 예외 발생
        Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                () -> new ResourceNotFoundException("카드", "카드번호", cardsDto.getCardNumber())
        );

        // DTO -> Entity 매핑 후 저장

        CardsMapper.mapToCards(cardsDto, cards);
        System.out.println("cardsDto?????2>"+ cardsDto);
        System.out.println("cards?????2>"+ cards);
        cardsRepository.save(cards);

        return true;
    }

    @Override
    public boolean deleteCard(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 휴대폰 번호로 카드 조회, 없으면 예외 발생
        Cards cards = cardsRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("카드", "휴대폰 번호", mobileNumber)
        );

        // 카드 삭제
        cardsRepository.deleteById(cards.getCardId());

        return true;
    }
}