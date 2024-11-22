package financetracker.controllers;

import financetracker.datatypes.Category;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.category.CreatingCategoryFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
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

    public long getCategoryId(String categoryName) throws SerializerCannotRead, IdNotFoundException {
        return modelSerializer.findId(
                new Category(-1, categoryName),
                (c1, c2) -> (c1.getName().compareTo(c2.getName())));
    }

    public Category getCategory(long id) throws SerializerCannotRead, CategoryLookupFailedException {
        for (Category category : modelSerializer.readAll()) {
            if (category.getId() == id) {
                return category;
            }
        }

        throw new CategoryLookupFailedException("Category with id of '" + id + "' was not found");
    }

    public Category createCategory(String categoryName) throws CreatingCategoryFailedException {
        Category result = new Category(modelSerializer.getNextId(), categoryName.toUpperCase());
        try {
            modelSerializer.appendNewData(result);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingCategoryFailedException("Failed to create new category due to IO Error", result);
        }

        return result;
    }
}
