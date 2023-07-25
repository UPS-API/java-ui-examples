// ***** Constants *****
const serviceCodeMap = {
  '01': 'UPS Next Day Air',
  '02': 'UPS 2nd Day Air',
  '03': 'UPS Ground',
  '07': 'UPS Worldwide Express',
  '08': 'UPS Worldwide Expedited',
  '11': 'UPS Standard',
  '12': 'UPS 3 Day Select',
  '13': 'UPS Next Day Air Saver',
  '14': 'UPS Next Day Air Early',
  '54': 'UPS Worldwide Express Plus',
  '59': 'UPS 2nd Day Air A.M.',
  '65': 'UPS Worldwide Saver'
}

// ******** Global Functions ********
function activateStep(idx) {
  dismissModal();
  updateProgress(idx);

  for (let i = 1; i <= 6; i++) {
    if (i == idx) {
      document.getElementById('step_' + i).classList.add('active');
    } else {
      document.getElementById('step_' + i).classList.remove('active')
    }
  }
}

function dismissModal() {
  document.querySelector(".modal_container").classList.add("hidden");
}

function showMissingFieldError(fields) {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let lis = [];
  Object.keys(fields).forEach(key => {
    if (fields[key] == '') {
      lis.push(`<li>Missing ${key}</li>`)
    }
  });
  let template = `
    <ul>
      ${lis.join('\n')}
    </ul>
  `

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function showUnderZeroErrors(fields) {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let lis = [];
  Object.keys(fields).forEach(key => {
    if (fields[key] < 0) {
      lis.push(`<li>${key} must be greater than 0.</li>`)
    }
  });
  let template = `
    <ul>
      ${lis.join('\n')}
    </ul>
  `

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function showMissingServiceError(fields) {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let template = `
    <ul>
      Please select a service
    </ul>
  `

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function showErrorModal(data) {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let template;
  if (data.errorResponse) {
    template = `
      <div class="modal-error_content">
        <h3>${data.statusCode}</h3>
        <div>Error Code: ${data.errorResponse.response.errors[0].code}</div>
        <div>Error message: ${data.errorResponse.response.errors[0].message}</div>
      </div>
    `
  } else if (data.statusCode == 500) {
    template = `
      <div class="modal-error_content">
        <h3>500</h3>
        <div>Please try again later</div>
      </div>
    `
  } else {
    template = `
      <div class="modal-error_content">
        <p>${data}</p>
      </div>
    `;
  }

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function showLoadingModal(message) {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let template = `
    <h3>${message}...<h3>
    <div id="loader-container">
      <div id="loader"></div>
    </div>
  `;

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function tntAvailable(alerts) {
  let tntAvailable = true;
  alerts.forEach(alert => {
    if (alert.Code == '111598') {
      tntAvailable = false;
    }
  })

  return tntAvailable;
}

function getDeliveryDetails(service) {
  let day = service.TimeInTransit.ServiceSummary.EstimatedArrival.DayOfWeek.toLowerCase();
  day = day.charAt(0).toUpperCase() + day.slice(1);
  let date = parseTNTDate(service.TimeInTransit.ServiceSummary.EstimatedArrival.Arrival.Date);
  let time = service.GuaranteedDelivery?.DeliveryByTime ?? 'End of Day';

  return `${day}, ${date} by ${time}`
}

function parseTNTDate(date) {
  return `${date.substring(4, 6)}/${date.substring(6)}/${date.substring(0, 4)}`;
}

function updateProgress(idx) {
  const progressBar = document.getElementById("progress-bar");
  const steps = document.querySelectorAll(".progress-step");

  steps.forEach((step, i) => {
    if (i < idx) {
      step.classList.add("active");
    } else {
      step.classList.remove("active");
    }
  });

  progressBar.style.width = ((idx - 1) / (steps.length - 1) * 100 + "%");
}

function resetApplication() {
  sessionStorage.clear();

  document.querySelectorAll('input[type="text"]').forEach(input => {
    input.value = "";
  })

  document.querySelectorAll('input[type="number"]').forEach(input => {
    input.value = "";
  })

  let packages = document.querySelectorAll('x-package');
  if (packages.length > 1) {
    packages.forEach(pkg => {
      if (pkg.dataset.index !== '1') {
        pkg.remove();
      }
    })
  }

  document.querySelectorAll('textarea').forEach(area => {
    area.value = "";
  })

  document.querySelectorAll('[id$=_countryCode]').forEach(el => el.value = 'US');


}

function logout() {
  sessionStorage.clear();
  document.getElementById("logoutForm").submit();
}


// ******** Shipper Step ********
function continueFromShipper() {
  const name = document.getElementById("shipper_name").value;
  const email = document.getElementById("shipper_email").value;
  const phone = document.getElementById("shipper_phone").value;
  const address1 = document.getElementById("shipper_addressLine1").value;
  const address2 = document.getElementById("shipper_addressLine2").value;
  const city = document.getElementById("shipper_city").value;
  const state = document.getElementById("shipper_state").value;
  const postal = document.getElementById("shipper_postalCode").value;
  const accNumber = document.getElementById("payment_number").value;

  // console.log("[Shipper] Fields: ", name, address1, address2, city, state, postal, accNumber);

  if (name != '' && address1 != '' && city != '' && state != '' && postal != '' && accNumber != '') {
    const shipperJson = {
      name, email, phone, address1, address2, city, state, postal, accNumber
    };
    sessionStorage.setItem("shipper", JSON.stringify(shipperJson));

    activateStep(2)
  } else {
    showMissingFieldError({
      "Name": name,
      "Address Line 1": address1,
      "City": city,
      "State": state,
      "Zipcode": postal,
      "Shipper / Account Number": accNumber
    });
  }
}


// ******** ShipTo / Recipient Step ********
function continueFromShipTo() {
  showLoadingModal("Validating Recipient Address");

  const name = document.getElementById("shipTo_name").value;
  const email = document.getElementById("shipTo_email").value;
  const phone = document.getElementById("shipTo_phone").value;
  const address1 = document.getElementById("shipTo_addressLine1").value;
  const address2 = document.getElementById("shipTo_addressLine2").value;
  const city = document.getElementById("shipTo_city").value;
  const state = document.getElementById("shipTo_state").value;
  const postal = document.getElementById("shipTo_postalCode").value;
  const validate = document.getElementById("shipTo_residential").checked;

  if (name != '' && address1 != '' && city != '' && state != '' && postal != '') {
    const shipToJson = {
      name, email, phone, address1, address2, city, state, postal, validate
    };
    sessionStorage.setItem("shipTo", JSON.stringify(shipToJson));

    let address = [address1];
    if (address2) {
      address.push(address2);
    }

    // Validate Address does not need email / phone / residential validation
    const validateAddressJson = {
      XAVRequest: {
        AddressKeyFormat: {
          ConsigneeName: name,
          AddressLine: address,
          PoliticalDivision2: city,
          PoliticalDivision1: state,
          PostcodePrimaryLow: postal,
          CountryCode: "US"
        }
      }
    }

    if (validate) {
      validateAddressJson.XAVRequest.RegionalRequestIndicator = "";
    }

    // console.log(validateAddressJson);

    const requestOptions = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(validateAddressJson)
    }

    fetch('/addressValidationAjax', requestOptions)
      .then(response => response.json())
      .then(data => {
        if (data.statusCode == 200 && data.statusMsg == "Success") {
          dismissModal();
          if (data.response.invalidAddress) {
            showInvalidModal();
          } else if (data.response.validAddress) {
            activateStep(3);
          } else if (data.response.ambiguousAddress) {
            showAmbiguousModal();
          }
        } else {
          showErrorModal(data);
        }
      })
      .catch(error => {
        showErrorModal("An error occured while trying to valiate the recipient's address.");
        console.error('[AddressValidation]', error)
      })

    // activateStep(3)
  } else {
    showMissingFieldError({
      "Name": name,
      "Address Line 1": address1,
      "City": city,
      "State": state,
      "Zipcode": postal
    });
  }
}

function showInvalidModal() {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let template = `
    <div>The inputted address is invalid, please input a valid address</div>
    <input class="remove-btn" value="Go Back" onClick="dismissModal()" />
  `;

  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}

function showAmbiguousModal() {
  let container = document.querySelector("#modal .modal_content");
  container.innerHTML = "";

  let template = `
    <div>We could not completely validate the inputted address.</div>
    <div>If you are sure about this address you may continue, otherwise please fix the address</div>
    <input class="remove-btn" value="Go Back" onClick="dismissModal()" />
    <input class="next-btn" value="Continue" onClick="activateStep(3)" />
    `;
    
  container.insertAdjacentHTML("afterbegin", template);
  document.querySelector(".modal_container").classList.remove("hidden");
}


// ******** Package Step ********
function continueFromPackage() {
  const packages = document.querySelectorAll("x-package");

  const packageInfo = [];
  const missingFieldErrors = {};
  const underZeroErrors = {};
  let showError = false;
  let showUnderZero = false;
  packages.forEach(pkg => {
    let idx = pkg.dataset.index;
    let description = document.getElementById(`shipment_description_${idx}`).value;
    let weight = document.getElementById(`shipment_weight_${idx}`).value;
    let length = document.getElementById(`shipment_length_${idx}`).value;
    let width = document.getElementById(`shipment_width_${idx}`).value;
    let height = document.getElementById(`shipment_height_${idx}`).value;
    let declared_value = document.getElementById(`shipment_dv_${idx}`).value;
    let ref1 = document.getElementById(`shipment_ref1_${idx}`).value;
    let ref2 = document.getElementById(`shipment_ref2_${idx}`).value

    // console.log('[Dimensions]', +length, +width, +height);

    if (description == '' || weight == '' || length == '' || width == '' || height == '') {
      missingFieldErrors[`Package ${idx} Description`] = description;
      missingFieldErrors[`Package ${idx} Weight`] = weight;
      missingFieldErrors[`Package ${idx} Length`] = length;
      missingFieldErrors[`Package ${idx} Width`] = width;
      missingFieldErrors[`Package ${idx} Height`] = height;
      showError = true;
    }
    if (+length <= 0 || +width <= 0 || +height <= 0) {
      underZeroErrors[`Package ${idx} Length`] = +length;
      underZeroErrors[`Package ${idx} Width`] = +width;
      underZeroErrors[`Package ${idx} Height`] = +height;
      showUnderZero = true;
    }
    if (declared_value != '' && +declared_value <= 0) {
      underZeroErrors[`Package ${idx} Declared Value`] = +declared_value;
      showUnderZero = true;
    }

    packageInfo.push({
      description, weight, length, width, height, declared_value, ref1, ref2
    });
  })

  // console.log(packageInfo);

  if (!showError && !showUnderZero) {
    sessionStorage.setItem("packages", JSON.stringify(packageInfo))

    getServices();
  } else if (showError) {
    showMissingFieldError(missingFieldErrors);
  } else if (showUnderZero) {
    showUnderZeroErrors(underZeroErrors);
  }
}

function getServices() {
  showLoadingModal("Getting shipping rates");

  const shipperInfo = JSON.parse(sessionStorage.getItem("shipper"));
  const shipToInfo = JSON.parse(sessionStorage.getItem("shipTo"));
  const packageInfo = JSON.parse(sessionStorage.getItem("packages"));

  let packages = [];
  packageInfo.forEach(pkg => {
    let json = {
      PackagingType: {
        Code: "02",
        Description: "Packaging"
      },
      Dimensions: {
        UnitOfMeasurement: {
          Code: "IN",
          Description: "Inches"
        },
        Length: pkg.length,
        Width: pkg.width,
        Height: pkg.height,
      },
      PackageWeight: {
        UnitOfMeasurement: {
          Code: "LBS",
          Description: "Pounds"
        },
        Weight: pkg.weight
      },
    }

    if (pkg.declared_value) {
      json.PackageServiceOptions = { ShipperDeclaredValue: pkg.declared_value };
    }

    packages.push(json);
  })

  const shipperAddress = shipperInfo.address2 ? `${shipperInfo.address1} ${shipperInfo.address2}` : shipperInfo.address1;
  const shipToAddress = shipToInfo.address2 ? `${shipToInfo.address1} ${shipToInfo.address2}` : shipToInfo.address1;

  const rateRequestJson = {
    RateRequest: {
      Request: {
        RequestOption: "Rate",
        SubVersion: "2108"
      },
      Shipment: {
        Shipper: {
          Name: shipperInfo.name,
          ShipperNumber: shipperInfo.accNumber,
          Address: {
            AddressLine: shipperAddress,
            City: shipperInfo.city,
            StateProvinceCode: shipperInfo.state,
            PostalCode: shipperInfo.postal,
            CountryCode: "US"
          }
        },
        ShipTo: {
          Name: shipToInfo.name,
          Address: {
            AddressLine: shipToAddress,
            City: shipToInfo.city,
            StateProvinceCode: shipToInfo.state,
            PostalCode: shipToInfo.postal,
            CountryCode: "US"
          }
        },
        Package: packages,
        NumOfPieces: packageInfo.length,
        DeliveryTimeInformation: {
          PackageBillType: "03"
        }, 
        ShipmentRatingOptions: {
          NegotiatedRatesIndicator: ""
        }
      }
    }
  }

  const requestOptions = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(rateRequestJson)
  }

  fetch('/rateAjax', requestOptions)
    .then(response => response.json())
    .then(data => {
      if (data.statusCode == 200 && data.statusMsg == "Success") {
        dismissModal();
        handleShippingServicesCall(data.response)
        activateStep(4);
      } else {
        showErrorModal(data);
      }
    })
    .catch(error => {
      showErrorModal("An error occured while trying to get service rates.");
      console.error('[Rate]', error)
    })
}

function handleShippingServicesCall(data) {
  if (data.Response.ResponseStatus.Code == '1' &&
      data.Response.ResponseStatus.Description == 'Success') {
        if (tntAvailable(data.Response.Alert)) {
          const container = document.getElementById("shipping_services");
          container.innerHTML = "";
          const allShipmentServices = data.RatedShipment.sort((a, b) => {
            return +b.TotalCharges.MonetaryValue - +a.TotalCharges.MonetaryValue;
          });
          allShipmentServices.forEach((service, idx) => {
            const template = document.createElement("div");
            template.classList.add("service-option");
            template.insertAdjacentHTML('beforeend', `
              <label>
                <input type="radio" class="service-radio" id="service-${idx}" value="${service.Service.Code}" ${service.Service.Code == '03' ? 'checked' : ''} name="service-radio" />
                <div>
                  <div class="service-price">$${service.TotalCharges.MonetaryValue}</div>
                  <div class="service-service">${service.TimeInTransit.ServiceSummary.Service.Description}</div>
                  <div class="service-date">${getDeliveryDetails(service)}</div>
                </div>
              </label>
              <br />
            `)
            template.service = service;
            container.insertAdjacentElement('beforeend', template);
          })
        
          // document.querySelector('input[name="service-radio"]:checked')?.closest(".service-option")?.service;
          document.querySelectorAll(".service-option").forEach(option => {
            option.addEventListener("click", function(evt) {
              document.querySelectorAll(".service-option input:not(:checked)").forEach(opt => {
                opt.closest(".service-option").classList.remove("active");
              })
              if (evt.target.checked) {
                evt.target.closest(".service-option").classList.add("active");
              }
            })
          })
        } else {
          const container = document.getElementById("shipping_services");
          container.innerHTML = "";
          const allShipmentServices = data.RatedShipment.sort((a, b) => {
            return +b.TotalCharges.MonetaryValue - +a.TotalCharges.MonetaryValue;
          });
          allShipmentServices.forEach((service, idx) => {
            const template = document.createElement("div");
            template.classList.add("service-option");
            let deliveryHTML = '';
            if (service.GuaranteedDelivery) {
              const guaranteedTime = service.GuaranteedDelivery.DeliveryByTime ?? 'End of Day';
              deliveryHTML = `<div class="service-business-days">Delivery in ${service.GuaranteedDelivery.BusinessDaysInTransit} business days by ${guaranteedTime}</div>`;
            }
            template.insertAdjacentHTML('beforeend', `
              <label>
                <input type="radio" class="service-radio" id="service-${idx}" value="${service.Service.Code}" ${service.Service.Code == '03' ? 'checked' : ''}  name="service-radio" />
                <div>
                  <div class="service-code">${serviceCodeMap[service.Service.Code]}</div>
                  ${deliveryHTML}
                  <div class="service-price">$${service.TotalCharges.MonetaryValue}</div>
                </div>
              </label>
              <br />
            `)
            template.service = service;
            container.insertAdjacentElement('beforeend', template);
          })
        
          // document.querySelector('input[name="service-radio"]:checked')?.closest(".service-option")?.service;
          document.querySelectorAll(".service-option").forEach(option => {
            option.addEventListener("click", function(evt) {
              document.querySelectorAll(".service-option input:not(:checked)").forEach(opt => {
                opt.closest(".service-option").classList.remove("active");
              })
              if (evt.target.checked) {
                evt.target.closest(".service-option").classList.add("active");
              }
            })
          })
        }
      }
}

function addNewPackage() {
  const packages = document.querySelectorAll("x-package");
  const packageIdx = +packages[packages.length - 1].dataset.index;

  const newPackageTemplate = `
    <hr>
    <x-package data-index="${packageIdx + 1}">
      <h2>Package ${packageIdx + 1}</h2>
      <div class="dap-form_input">
        <label for="shipment_description_${packageIdx + 1}">Package Description*</label>
        <textarea id="shipment_description_${packageIdx + 1}" rows="4"></textarea>
      </div>
      <div class="dap-form_input">
        <label for="shipment_weight_${packageIdx + 1}">Shipment Weight*</label>
        <div>
          <input id="shipment_weight_${packageIdx + 1}" type="number" />
          <label for="shipment_weight_${packageIdx + 1}">Lbs</label>
        </div>
      </div>
      <div>
        <h3>Package Dimensions</h3>
        <div>
          <div class="dap-form_input package-form_dim_input">
            <label for="shipment_length_${packageIdx + 1}">Length*</label>
            <div>
              <input id="shipment_length_${packageIdx + 1}" type="number" />
              <label for="shipment_length_${packageIdx + 1}">in</label>
            </div>
          </div>
          <div class="dap-form_input package-form_dim_input">
            <label for="shipment_width_${packageIdx + 1}">Width*</label>
            <div>
              <input id="shipment_width_${packageIdx + 1}" type="number" />
              <label for="shipment_width_${packageIdx + 1}">in</label>
            </div>
          </div>
          <div class="dap-form_input package-form_dim_input">
            <label for="shipment_height_${packageIdx + 1}">Height*</label>
            <div>
              <input id="shipment_height_${packageIdx + 1}" type="number" />
              <label for="shipment_height_${packageIdx + 1}">in</label>
            </div>
          </div>
        </div>
      </div>
      <div>
        <h3>Package Declared Value (Package Value)</h3>

        <div class="dap-form_input">
          <label for="shipment_dv_${packageIdx + 1}">UPS's liability for loss or damage is limited to US$100 without a declaration of value (limit may vary for non-U.S. origin shipments). To increase UPS’s liability, you must declare a higher value and pay an additional charge.</label>
          <input id="shipment_dv_${packageIdx + 1}" type="number" />USD
        </div>
      </div>
      <div>
        <h3>Reference Numbers</h3>

        <div class="dap-form_input">
          <label for="shipment_ref1_${packageIdx + 1}">Reference Number 1</label>
          <input id="shipment_ref1_${packageIdx + 1}" type="text" />
        </div><div class="dap-form_input">
          <label for="shipment_ref2_${packageIdx + 1}">Reference Number 2</label>
          <input id="shipment_ref2_${packageIdx + 1}" type="text" />
        </div>
      </div>
    </x-package>
  `

  packages[packages.length - 1].insertAdjacentHTML('afterend', newPackageTemplate);
}

function removeLastPackage() {
  const packages = document.querySelectorAll("x-package");
  const lastPackage = packages[packages.length - 1];
  const packageIdx = +lastPackage.dataset.index;

  if (packageIdx > 1) {
    lastPackage.previousElementSibling.remove();
    lastPackage.remove();
  }
}


// ******** Services Page ********
function continueFromService() {
  const selectedService = document.querySelector('input[name="service-radio"]:checked')?.closest(".service-option")?.service;

  if (selectedService) {
    sessionStorage.setItem("service", JSON.stringify(selectedService));

    populateReviewDetails();
    activateStep(5);
  } else {
    showMissingServiceError({
      "Service": selectedService
    });
  }
}

function populateReviewDetails() {
  const shipperInfo = JSON.parse(sessionStorage.getItem("shipper"));
  const shipToInfo = JSON.parse(sessionStorage.getItem("shipTo"));
  const packageInfo = JSON.parse(sessionStorage.getItem("packages"));
  const serviceInfo = JSON.parse(sessionStorage.getItem("service"));

  const shipperTemplate = `
    <h3>Shipper</h3>
    <div>${shipperInfo["name"]}</div>
    <div>${shipperInfo["email"]}</div>
    <div>${shipperInfo["phone"]}</div>

    <div>${shipperInfo["address1"]}${shipperInfo["address2"] ? " " + shipperInfo["address2"] : ""}</div>
    <div>${shipperInfo["city"]}, ${shipperInfo["state"]}</div>
    <div>${shipperInfo["postal"]}</div>
  `
  document.getElementById("review_shipper").innerHTML = shipperTemplate;
  // document.getElementById("review_shipper").insertAdjacentHTML("afterbegin", shipperTemplate);
  
  const shipToTemplate = `
    <h3>ShipTo</h3>
    <div>${shipToInfo["name"]}</div>
    <div>${shipToInfo["email"]}</div>
    <div>${shipToInfo["phone"]}</div>

    <div>${shipToInfo["address1"]}${shipToInfo["address2"] ? " " + shipToInfo["address2"] : ""}</div>
    <div>${shipToInfo["city"]}, ${shipToInfo["state"]}</div>
    <div>${shipToInfo["postal"]}</div>
  `
  document.getElementById("review_shipTo").innerHTML = shipToTemplate;
  // document.getElementById("review_shipTo").insertAdjacentHTML("afterbegin", shipToTemplate);

  const accountNumTemplate = `
    <h3>Billing Account Number</h3>
    <div>${shipperInfo["accNumber"]}</div>
  `
  document.getElementById("review_account_number").innerHTML = accountNumTemplate;

  let serviceTemplate;
  if (serviceInfo.TimeInTransit) {
    serviceTemplate = `
      <h3>Selected Service</h3>
      <div>${serviceInfo.TimeInTransit.ServiceSummary.Service.Description}</div>
      <div>Deliver by ${getDeliveryDetails(serviceInfo)}</div>
      <div>$${serviceInfo.TotalCharges.MonetaryValue}</div>
    `
  } else {
    let deliveryHTML = '';
    if (serviceInfo.GuaranteedDelivery) {
      const guaranteedTime = serviceInfo.GuaranteedDelivery.DeliveryByTime ?? 'End of Day';
      deliveryHTML = `<div class="service-business-days">Delivery in ${serviceInfo.GuaranteedDelivery.BusinessDaysInTransit} business days by ${guaranteedTime}</div>`;
    }
    serviceTemplate = `
      <h3>Selected Service</h3>
      <div>${serviceCodeMap[serviceInfo.Service.Code]}</div>
      ${deliveryHTML}
      <div>$${serviceInfo.TotalCharges.MonetaryValue}</div>
    `
  }
  document.getElementById("review_services").innerHTML = serviceTemplate;

  packageDivs = [];
  packageInfo.forEach((pkg, idx) => {
    packageDivs.push(`
      <div class="accordion-container">
        <button class="accordion">Package ${idx + 1}</button>
        <span>&#9660;</span>
      </div>
      <div class="panel">
        <div class="review_pkg-description">Package Description: ${pkg.description}</div>
        <div class="review_pkg-weight">Package Weight: ${pkg.weight} Lbs</div>
        <div class="review_lwh">Dimensions (LxWxH): ${pkg.length}x${pkg.width}x${pkg.height}</div>
      </div>
    `)
  })

  const packageTemplate = `
    <h3>Packages</h3>
    ${packageDivs.join('\n')}
  `
  document.getElementById("review_packages").innerHTML = packageTemplate;
  document.querySelectorAll("#step_5 .accordion-container").forEach(accordion => {

    accordion.addEventListener("click", function() {
      accordion.classList.toggle("accordion-active");
      if (accordion.classList.contains("accordion-active")) {
        accordion.querySelector("span").innerHTML = "&#9650;"
      } else {
        accordion.querySelector("span").innerHTML = "&#9660;"
      }
    })
  });

  // console.log(
  //   "[Shipper]", JSON.parse(sessionStorage.getItem("shipper")),
  //   "[ShipTo]", JSON.parse(sessionStorage.getItem("shipTo")),
  //   "[Package]", JSON.parse(sessionStorage.getItem("packages")),
  //   "[Service]", JSON.parse(sessionStorage.getItem("service"))
  // )
}


// ******** Review Page ********
function makeShipment() {
  showLoadingModal("Creating Shipment");

  const shipperInfo = JSON.parse(sessionStorage.getItem("shipper"));
  const shipToInfo = JSON.parse(sessionStorage.getItem("shipTo"));
  const packageInfo = JSON.parse(sessionStorage.getItem("packages"));
  const serviceInfo = JSON.parse(sessionStorage.getItem("service"));

  let packages = [];
  packageInfo.forEach(pkg => {
    let json = {
      Packaging: {
        Code: "02",
        Description: "Packaging"
      },
      Dimensions: {
        UnitOfMeasurement: {
          Code: "IN",
          Description: "Inches"
        },
        Length: pkg.length,
        Width: pkg.width,
        Height: pkg.height,
      },
      PackageWeight: {
        UnitOfMeasurement: {
          Code: "LBS",
          Description: "Pounds"
        },
        Weight: pkg.weight
      },
    }

    if (pkg.declared_value) {
      json.PackageServiceOptions = { DeclaredValue: { CurrencyCode: "USD", MonetaryValue: pkg.declared_value } };
    }

    let references = pkg.ref2 ? `${pkg.ref1} ${pkg.ref2}` : pkg.ref1;

    // json.ReferenceNumber = { Value: references };

    packages.push(json);
  })

  const shipperAddress = shipperInfo.address2 ? `${shipperInfo.address1} ${shipperInfo.address2}` : shipperInfo.address1;
  const shipToAddress = shipToInfo.address2 ? `${shipToInfo.address1} ${shipToInfo.address2}` : shipToInfo.address1;

  const shipmentJson = {
    ShipmentRequest: {
      Request: {
        SubVersion: "1801",
        RequestOption: "nonvalidate",
        TransactionReference: {
          CustomerContext: ""
        }
      },
      Shipment: {
        Description: "1206 PTR",
        Shipper: {
          Name: shipperInfo['name'],
          ShipperNumber: shipperInfo['accNumber'],
          Address: {
            AddressLine: shipperAddress,
            City: shipperInfo['city'],
            StateProvinceCode: shipperInfo['state'],
            PostalCode: shipperInfo['postal'],
            CountryCode: "US"
          }
        },
        ShipTo: {
          Name: shipToInfo['name'],
          Address: {
            AddressLine: shipToAddress,
            City: shipToInfo['city'],
            StateProvinceCode: shipToInfo['state'],
            PostalCode: shipToInfo['postal'],
            CountryCode: "US"
          }
        },
        PaymentInformation: {
          ShipmentCharge: {
            Type: "01",
            BillShipper: {
              AccountNumber: shipperInfo['accNumber']
            }
          }
        },
        Service: {
          Code: serviceInfo.Service.Code,
          Description: serviceInfo.Service.Description
        },
        Package: packages,
        ShipmentRatingOptions: {
          NegotiatedRatesIndicator: ""
        }
      },
      LabelSpecification: {
        LabelImageFormat: {
          Code: "GIF"
        }
      }
    }
  }

  // console.log('shipmentJson', shipmentJson);

  const requestOptions = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(shipmentJson)
  }

  fetch('/shipAjax', requestOptions)
    .then(res => res.json())
    .then(res => {
      if (res.statusCode == 200 && res.statusMsg == "Success") {
        dismissModal();
        handleShipmentResponse(res.response);
        activateStep(6);
      } else {
        showErrorModal(res);
      }
    })
    .catch(error => {
      showErrorModal("An error occured while trying to create a shipment label.");
      console.error('[Shipment]', error)
    })
}

function handleShipmentResponse(res) {
  if (res.Response.ResponseStatus.Code != "1" && res.Response.ResponseStatus.Description != "Success") {
    console.error('Could not make shipment request');
  } else {
    sessionStorage.setItem('shipment_confirmation', JSON.stringify(res));
    const shipperInfo = JSON.parse(sessionStorage.getItem("shipper"));
    const shipToInfo = JSON.parse(sessionStorage.getItem("shipTo"));
    const packageInfo = JSON.parse(sessionStorage.getItem("packages"));
    const serviceInfo = JSON.parse(sessionStorage.getItem("service"));

    document.getElementById("confirm_trackingNum").innerHTML = res.ShipmentResults.ShipmentIdentificationNumber;
    document.getElementById("confirm_price").innerHTML = res.ShipmentResults.ShipmentCharges.TotalCharges.MonetaryValue;
    
    const constituentTemplate = `
      <div>
        <h3>Shipper</h3>
        <div>${shipperInfo["name"]}</div>
        <div>${shipperInfo["email"]}</div>
        <div>${shipperInfo["phone"]}</div>

        <div>${shipperInfo["address1"]}${shipperInfo["address2"] ? " " + shipperInfo["address2"] : ""}</div>
        <div>${shipperInfo["city"]}, ${shipperInfo["state"]}</div>
        <div>${shipperInfo["postal"]}</div>
      </div>
      <div>
        <h3>ShipTo</h3>
        <div>${shipToInfo["name"]}</div>
        <div>${shipToInfo["email"]}</div>
        <div>${shipToInfo["phone"]}</div>
    
        <div>${shipToInfo["address1"]}${shipToInfo["address2"] ? " " + shipToInfo["address2"] : ""}</div>
        <div>${shipToInfo["city"]}, ${shipToInfo["state"]}</div>
        <div>${shipToInfo["postal"]}</div>
      </div>
    `;
    document.getElementById("confirmation_constituents").innerHTML = constituentTemplate;

    const accountNumTemplate = `
      <h3>Billing Account Number</h3>
      <div>${shipperInfo["accNumber"]}</div>
    `
    document.getElementById("confirmation_billing").innerHTML = accountNumTemplate;

    const serviceTemplate = `
      <h3>Selected Service</h3>
      <div>${serviceInfo.TimeInTransit.ServiceSummary.Service.Description}</div>
      <div>Deliver by ${getDeliveryDetails(serviceInfo)}</div>
      <div>$${serviceInfo.TotalCharges.MonetaryValue}</div>
    `
    document.getElementById("confirmation_services").innerHTML = serviceTemplate;

    packageDivs = [];
    packageInfo.forEach((pkg, idx) => {
      packageDivs.push(`
        <div class="accordion-container">
          <button class="accordion">Package ${idx + 1}</button>
          <span>&#9660;</span>
        </div>
        <div class="panel">
          <div class="confirmation_pkg-description">Package Description: ${pkg.description}</div>
          <div class="confirmation_pkg-weight">Package Weight: ${pkg.weight} Lbs</div>
          <div class="confirmation_lwh">Dimensions (LxWxH): ${pkg.length}x${pkg.width}x${pkg.height}</div>
        </div>
      `)
    })

    const packageTemplate = `
      <h3>Packages</h3>
      ${packageDivs.join('\n')}
    `
    document.getElementById("confirmation_packages").innerHTML = packageTemplate;
    document.querySelectorAll("#step_6 .accordion-container").forEach(accordion => {
  
      accordion.addEventListener("click", function() {
        accordion.classList.toggle("accordion-active");
        if (accordion.classList.contains("accordion-active")) {
          accordion.querySelector("span").innerHTML = "&#9650;"
        } else {
          accordion.querySelector("span").innerHTML = "&#9660;"
        }
      })
    });

    resetApplication();
  }
}