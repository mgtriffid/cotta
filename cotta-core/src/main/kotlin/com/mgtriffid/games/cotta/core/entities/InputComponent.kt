package com.mgtriffid.games.cotta.core.entities

/**
 * When added to an Entity, first should be blank.
 * entity.addInput(InputComponent::class)
 * Then should be filled with some data
 * entity.getInput(inputComponent).setValue(inputComponentValue)
 * By default is blank _whatever it means_
 * Upon advancing state is either blank or previous version, up to dev
 */
// TODO doesn't have to be parameterized
interface InputComponent<T: InputComponent<T>>
