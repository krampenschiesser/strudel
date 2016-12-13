/**
 * Copyright [2016] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE_2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.strudel.route;

public enum HttpStatus {
  //100
  CONTINUE(100),
  SWITCHING_PROTOCOLS(100),
  PROCESSING(100),
  //200
  OK(200),
  CREATED(201),
  ACCEPTED(202),
  NON_AUTHORITIVE_INFORMATION(203),
  NO_CONTENT(204),
  RESET_CONTENT(205),
  PARTIAL_CONTENT(206),
  MULTI_STATUS(207),
  ALREADY_REPORTED(208),
  IM_USED(226),
  //300
  MULTIPLE_CHOICES(300),
  MOVED_PERMANENTLY(301),
  MOVED_TEMPORARILY(302),
  SEE_OTHER(303),
  NOT_MODIFIED(304),
  USE_PROXY(305),
  SWITCH_PROXY(306),
  TEMPORARY_REDIRECT(307),
  PERMANENT_REDIRECT(308),
  //400
  BAD_REQUEST(400),
  UNAUTHERIZED(401),
  PAYMENT_REQUIRED(402),
  FORBIDDEN(403),
  NOT_FOUND(404),
  METHOD_NOT_ALLOWED(405),
  NOT_ACCEPTABLE(406),
  PROXY_AUTH_REQUIRED(407),
  REQUEST_TIME_OUT(408),
  CONFLICT(409),
  GONE(410),
  LENGTH_REQUIRED(411),
  PRECONDITION_FAILED(412),
  REQUEST_ENTITY_TOO_LARGE(413),
  REQUEST_URL_TOO_LONG(414),
  UNSUPPORTED_MEDIA_TYPE(415),
  REQUEST_RANGE_NOT_SATISFIABLE(416),
  EXCEPTION_FAILED(417),
  POLICY_NOT_FULFILLED(420),
  MISDIRECTED_REQUEST(421),
  UNPROCESSABLE_ENTITY(422),
  LOCKED(423),
  FAILED_DEPENEDENCY(424),
  UNORDERED_COLLECTION(425),
  UPGRADE_REQUIRED(426),
  PRECONDITION_REQUIRED(428),
  TOO_MANY_REQUESTS(429),
  REQUEST_HEADER_FIELDS_TO_LARGE(431),
  UNAVAILABLE_FOR_LEGAL_REASONS(451),
  NO_RESPONSE(444),
  MS_EXCHANGE(449),
  //500
  INTERNAL_SERVER_ERROR(500),
  NOT_IMPLEMENTED(501),
  BAD_GATEWAY(502),
  SERVICE_UNAVAILABLE(503),
  GATEWAY_TIME_OUT(504),
  HTTP_VERSION_NOT_SUPPORTED(505),
  VARIANT_ALSO_NEGOTIATES(506),
  INSUFFICIENT_STORAGE(507),
  LOOP_DETECTED(508),
  BANDWIDTH_LIMIT_EXCEEDED(509),
  NOT_EXTENDED(510),
  NETWORK_AUTH_REQUIRED(511);

  private final int value;

  HttpStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
