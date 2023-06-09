let SHOP_SERVER;
let FRONT_SERVER;
const RESPONSE_KEY = "response";

const parentCategories = []
const categories = {};
let activeCategoryId;
let activeIsbn;

function addEventListenerToCouponDuration() {
  const couponDurationRadioList = document.querySelectorAll(
      ".coupon-duration-type-radio");
  const couponDurationInput = document.querySelector("#coupon-duration-input");
  const couponDurationStartDateInput = document.querySelector(
      "#coupon-duration-start-date-input");
  const couponDurationEndDateInput = document.querySelector(
      "#coupon-duration-end-date-input");
  const couponDurationTypeDurationRadio = document.querySelector(
      "#coupon-duration-type-duration");
  const couponDurationTypeDateRadio = document.querySelector(
      "#coupon-duration-type-date");
  couponDurationRadioList.forEach(
      radio => radio.addEventListener("click", () => {
        couponDurationInput.disabled = true;
        couponDurationStartDateInput.disabled = true;
        couponDurationEndDateInput.disabled = true;

        if (couponDurationTypeDurationRadio.checked) {
          couponDurationInput.disabled = false;
        } else if (couponDurationTypeDateRadio.checked) {
          couponDurationStartDateInput.disabled = false;
          couponDurationEndDateInput.disabled = false;
        }
      }));
}

function addEventListenerToCouponType() {
  const couponTypeRadioList = document.querySelectorAll(".coupon-type-radio")
  const couponTypePointRadio = document.querySelector("#POINT");
  const couponTypeRateRadio = document.querySelector("#FIXED_RATE");
  const couponMaxDiscountPriceDiv = document.querySelector(
      "#coupon-max-discount-price-div");
  const couponMinOrderPriceDiv = document.querySelector(
      "#coupon-min-order-price-div");
  const couponBoundSelectDiv = document.querySelector(
      "#coupon-bound-select-div");
  couponTypeRadioList.forEach(radio => radio.addEventListener("click", () => {
    couponBoundSelectDiv.style.display = "";
    couponMinOrderPriceDiv.style.display = "";
    couponMaxDiscountPriceDiv.style.display = "none";
    couponMinOrderPriceDiv.querySelector("input").disabled = false;
    couponMaxDiscountPriceDiv.querySelector("input").disabled = true;
    couponBoundSelectDiv.querySelector("select").disabled = false;
    if (couponTypePointRadio.checked) {
      couponBoundSelectDiv.style.display = "none";
      couponBoundSelectDiv.querySelector("select").disabled = true;
      couponMinOrderPriceDiv.style.display = "none";
      couponMinOrderPriceDiv.querySelector("input").disabled = true;
      couponMaxDiscountPriceDiv.style.display = "none";
      couponMaxDiscountPriceDiv.querySelector("input").disabled = true;
    } else if (couponTypeRateRadio.checked) {
      couponMaxDiscountPriceDiv.style.display = "";
      couponMaxDiscountPriceDiv.querySelector("input").disabled = false;
    }
  }));
}

function addEventListenerToCouponQuantity() {
  const unlimitedQuantityCheckbox = document.querySelector(
      "#coupon-unlimited-quantity-check");
  const quantityInput = document.querySelector("#coupon-quantity-input");
  unlimitedQuantityCheckbox.addEventListener("click", () => {
    quantityInput.disabled = unlimitedQuantityCheckbox.checked;
  })
}

function initActiveParentCategory() {
  const parentCategoryItems = document.querySelectorAll(
      ".parent-category-item");
  parentCategoryItems.forEach(c => c.classList.remove("active"));
}

function initActiveChildCategory() {
  const childCategoryItems = document.querySelectorAll(".child-category-item");
  childCategoryItems.forEach(c => c.classList.remove("active"));
}

function addEventListenerToChildCategoryItems() {
  const childCategoryItems = document.querySelectorAll(".child-category-item");
  childCategoryItems.forEach(c => c.addEventListener('click', async () => {
    initActiveChildCategory();
    activeCategoryId = c.dataset['categoryId'];
    c.classList.add("active");
  }));
}

function addChildCategoryItemsToDiv(parentCategoryId) {
  const childrenCategoryGroup = document.querySelector(
      "#children-category-group");
  childrenCategoryGroup.innerHTML = "";

  categories[parentCategoryId].data.forEach(c => {
    const item = document.createElement("li");
    item.classList.add("list-group-item", "child-category-item");
    item.textContent = c.name;
    item.dataset['categoryId'] = c.id;
    childrenCategoryGroup.appendChild(item);
  })
  addEventListenerToChildCategoryItems();
}

function addEventListenerToParentCategoryItems() {
  const parentCategoryItems = document.querySelectorAll(
      ".parent-category-item");
  parentCategoryItems.forEach(c => c.addEventListener('click', async () => {
    const parentCategoryId = c.dataset['categoryId'];
    initActiveParentCategory();
    initActiveChildCategory();
    activeCategoryId = parentCategoryId;
    c.classList.add("active");
    if (!categories[parentCategoryId]) {
      const response = await fetch(
          `${SHOP_SERVER}/v1/categories/${parentCategoryId}?cate=children`);
      categories[parentCategoryId] = await response.json();
    }
    addChildCategoryItemsToDiv(parentCategoryId);
  }));
}

function addParentCategoryItemsToDiv() {
  const parentCategoryGroup = document.querySelector("#parent-category-group");
  parentCategoryGroup.innerHTML = "";

  parentCategories.forEach(c => {
    const item = document.createElement("li");
    item.classList.add("list-group-item", "parent-category-item");
    item.textContent = c.name;
    item.dataset['categoryId'] = c.id;
    parentCategoryGroup.appendChild(item);
  });
  addEventListenerToParentCategoryItems();
}

async function initParentCategories() {
  if (parentCategories.length > 0) {
    addParentCategoryItemsToDiv()
    return;
  }
  try {
    const response = await fetch(`${SHOP_SERVER}/v1/categories?cate=parents`);
    const parsedBody = await response.json();
    parsedBody.data.forEach(c => parentCategories.push(c));
    addParentCategoryItemsToDiv()
  } catch (e) {
    console.error(e);
    alert("카테고리 목록을 불러올 수 없습니다.")
  }
}

function addEventListenerToSearchedItems() {
  const searchedItems = document.querySelectorAll('.searched-list-item');
  searchedItems.forEach(item => item.addEventListener('click', (event) => {
    searchedItems.forEach(
        searchedItem => searchedItem.classList.remove('active'));
    event.target.classList.add('active');
  }))
}

function addEventListenerToCouponBound() {
  const couponBoundSelect = document.querySelector("#coupon-bound-select");
  const categorySelectDiv = document.querySelector("#category-select-div");
  const searchContainer = document.querySelector('#search-container')
  couponBoundSelect.addEventListener("change", async () => {
    categorySelectDiv.style.display = 'none';
    searchContainer.style.display = 'none'
    const value = couponBoundSelect.options[couponBoundSelect.selectedIndex].value;
    if (value === 'CATEGORY') {
      activeCategoryId = null;
      await initParentCategories();
      categorySelectDiv.style.display = '';
    } else if (value === 'PRODUCT') {
      searchContainer.style.display = '';
      addEventListenerToSearchedItems();
    }
  })
}

function handleSubmitEvent() {
  const form = document.querySelector('#coupon-create-form');
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData(form);
    if (formData.get("couponBoundCode") === "CATEGORY") {
      if (!activeCategoryId) {
        alert("카테고리가 선택되지 않았습니다.")
        return;
      }
      formData.append("categoryId", activeCategoryId);
    } else if (formData.get("couponBoundCode") === "PRODUCT") {
      if (!activeIsbn) {
        alert("상품이 선택되지 않았습니다.")
        return;
      }
      formData.append("isbn", activeIsbn);
    }

    const response = await fetch(`${FRONT_SERVER}/manager/coupon/create`,
        {method: "POST", body: formData});
    const parsedResponse = await response.json();
    localStorage.setItem(RESPONSE_KEY, JSON.stringify(parsedResponse));
    location.reload();
  })
}

function initConnectionInfo() {
  SHOP_SERVER = document.querySelector("#shop-server-url").textContent
  FRONT_SERVER = document.querySelector("#front-server-url").textContent
}

function showSuccessAlert(name) {
  const alertDiv = document.querySelector("#coupon-created-alert");
  alertDiv.textContent = `쿠폰 ${name}이(가) 생성되었습니다.`;
  alertDiv.style.display = '';
  localStorage.removeItem(RESPONSE_KEY);
}

function errorAlert(errorMessageList) {
  const alertDiv = document.querySelector("#error-alert");
  const ul = document.createElement("ul");
  errorMessageList.forEach(em => {
    const li = document.createElement("li");
    li.textContent = em;
    ul.appendChild(li);
  })
  alertDiv.appendChild(ul);
  alertDiv.style.display = '';
  localStorage.removeItem(RESPONSE_KEY);
}

function initAlert() {
  const responseData = localStorage.getItem(RESPONSE_KEY);
  if (!responseData) {
    return;
  }

  const parsedResponseData = JSON.parse(responseData);
  if (parsedResponseData.name) {
    return showSuccessAlert(parsedResponseData.name);
  }
  return errorAlert(parsedResponseData.errorMessageList);
}

function addEventListenerToCouponTriggerCode() {
  const couponOfMonthMetaInput = document.querySelector(
      '#coupon-of-month-only');
  const couponTriggerSelect = document.querySelector('#coupon-trigger-select');
  const couponDurationInput = document.querySelector("#coupon-duration-input");
  const couponDurationStartDateInput = document.querySelector(
      "#coupon-duration-start-date-input");
  const couponDurationEndDateInput = document.querySelector(
      "#coupon-duration-end-date-input");
  const duration = document.querySelector('#duration');
  const expireDate = document.querySelector('#expire-date');
  const unlimitedQuantityCheckbox = document.querySelector(
      "#coupon-unlimited-quantity-check");
  const quantityInput = document.querySelector("#coupon-quantity-input");

  unlimitedQuantityCheckbox.checked = true;
  quantityInput.disabled = true;

  couponTriggerSelect.addEventListener("change", async () => {
    couponDurationInput.disabled = true;
    couponDurationStartDateInput.disabled = true;
    couponDurationEndDateInput.disabled = true;

    if (couponTriggerSelect.options[couponTriggerSelect.selectedIndex].value
        === 'COUPON_OF_THE_MONTH') {
      couponOfMonthMetaInput.style.display = 'block';
      unlimitedQuantityCheckbox.checked = false;
      quantityInput.disabled = false;
    } else {
      couponOfMonthMetaInput.style.display = 'none';
      unlimitedQuantityCheckbox.checked = true;
      quantityInput.disabled = true;
    }

    if (couponTriggerSelect.options[couponTriggerSelect.selectedIndex].value
        === 'SIGN_UP'
        || couponTriggerSelect.options[couponTriggerSelect.selectedIndex].value
        === 'BIRTHDAY') {
      couponDurationInput.disabled = false;
      expireDate.style.display = 'none';
      duration.style.display = 'block';
    } else {
      couponDurationStartDateInput.disabled = false;
      couponDurationEndDateInput.disabled = false;
      expireDate.style.display = 'block';
      duration.style.display = 'none';
    }
  });
}

async function searchProductByTitle(title) {
  const response = await fetch(
      `${SHOP_SERVER}/v1/search/products?title=${title}&size=20&offset=0`);
  const parsedResponse = (await response.json()).data;

  return parsedResponse.dataList;
}

function initActiveIsbn() {
  activeIsbn = null;
  const searchedItem = document.querySelectorAll('.searched-list-item');
  searchedItem.forEach(item => item.classList.remove('active'));
}

function addSearchedProductList(dataList) {
  const searchedListGroup = document.querySelector("#searched-list-group");
  searchedListGroup.innerHTML = '';
  dataList.forEach(data => {
    const li = document.createElement('li');
    li.classList.add('list-group-item', 'searched-list-item');
    li.textContent = data.title;
    li.addEventListener('click', (e) => {
      initActiveIsbn();
      activeIsbn = data.isbn;
      e.target.classList.add('active');
    })
    searchedListGroup.appendChild(li);
  });
}

function addEventListenerToSearchBoxAndButton() {
  const searchButton = document.querySelector('#product-search-button');
  const searchBar = document.querySelector('#product-search-bar');
  const callback = async () => {
    console.log(searchBar.value);
    initActiveIsbn();
    const dataList = await searchProductByTitle(searchBar.value);
    addSearchedProductList(dataList);
  };

  searchButton.addEventListener('click', callback);
  searchBar.addEventListener('keypress', (event) => {
    if (event.code === 'Enter') {
      event.preventDefault();
      searchButton.click();
    }
  })
}

(function init() {
  initAlert();
  addEventListenerToSearchBoxAndButton();
  addEventListenerToCouponTriggerCode();
  addEventListenerToCouponQuantity();
  addEventListenerToCouponType();
  addEventListenerToCouponDuration();
  addEventListenerToCouponBound();
  handleSubmitEvent();
  initConnectionInfo();
})();
