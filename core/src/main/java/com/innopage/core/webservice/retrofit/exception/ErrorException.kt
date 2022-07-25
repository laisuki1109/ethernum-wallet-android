package com.innopage.core.webservice.retrofit.exception

import com.innopage.core.webservice.model.Error
import com.innopage.core.webservice.model.ErrorField

class ErrorException(val error: Error?, val errorFields: List<ErrorField>?) : Exception()