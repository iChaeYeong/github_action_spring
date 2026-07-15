package com.app.mono.cards.service;


import com.app.mono.cards.dto.CardsDto;

public interface CardsService {


    void createCard(String mobileNumber);

    CardsDto fetchCard(String mobileNumber);

    boolean updateCard(CardsDto cardsDto);

    boolean deleteCard(String mobileNumber);

}
