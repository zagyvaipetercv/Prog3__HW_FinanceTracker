package financetracker.controllers;

import java.util.ArrayList;
import java.util.List;

import financetracker.datatypes.Category;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.windowing.MainFrame;

public class CategoryController extends Controller<Category> {
    private static final String DEFAULT_SAVE_PATH = "saves/categories.dat";

    public CategoryController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public CategoryController(String savePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(savePath, mainFrame);
    }

    public long getCategoryId(String categoryName) throws CategoryLookupFailedException, IdNotFoundException {
        try {
            return modelSerializer.findId(
                    new Category(-1, categoryName),
                    (c1, c2) -> (c1.getName().compareTo(c2.getName())));
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find id for cotegory");
        }
    }

    public Category getCategory(String categoryName) throws CategoryLookupFailedException {
        try {
            for (Category category : modelSerializer.readAll()) {
                if (category.getName().equals(categoryName)) {
                    return category;
                }
            }
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find cateogry due to an IO Error");
        }

        throw new CategoryLookupFailedException("Category with name of '" + categoryName + "' was not found");
    }

    public Category getCategory(long id) throws CategoryLookupFailedException {
        try {
            for (Category category : modelSerializer.readAll()) {
                if (category.getId() == id) {
                    return category;
                }
            }
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to find category due to an IO Error");
        }

        throw new CategoryLookupFailedException("Category with id of '" + id + "' was not found");
    }

    public List<String> getCategoriesNames() throws CategoryLookupFailedException {
        try {
            List<Category> allCategories = modelSerializer.readAll();
            List<String> categoryNames = new ArrayList<>();

            for (Category category : allCategories) {
                categoryNames.add(category.getName());
            }

            return categoryNames;
        } catch (SerializerCannotRead e) {
            throw new CategoryLookupFailedException("Failed to lookup categories");
        }
    }

    public Category createCategory(String categoryName) throws CreatingRecordFailed {
        Category result = new Category(modelSerializer.getNextId(), categoryName.toUpperCase());
        try {
            modelSerializer.appendNewData(result);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingRecordFailed("Failed to create new category due to IO Error", result);
        }

        return result;
    }
}
