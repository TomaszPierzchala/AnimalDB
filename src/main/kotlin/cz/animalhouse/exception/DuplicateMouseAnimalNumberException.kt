package cz.animalhouse.exception

class DuplicateMouseAnimalNumberException(
    animalNumber: Int
) : RuntimeException(
    "Mouse with animal number=$animalNumber already exists"
)