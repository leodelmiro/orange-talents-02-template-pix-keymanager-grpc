package br.com.leodelmiro.compartilhado.validacao

import io.grpc.Status
import io.grpc.stub.StreamObserver


fun StreamObserver<out Any>.errorResponse(status: Status, errorMessage: ErrorMessage) {
    if (errorMessage.augmentDescription == null) {
        this.onError(status
                .withDescription(errorMessage.description)
                .asRuntimeException()
        )

        return
    }

    this.onError(status
            .withDescription(errorMessage.description)
            .augmentDescription(errorMessage.augmentDescription)
            .asRuntimeException()
    )
}